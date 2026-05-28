import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { MovimientoService } from '../../core/services/movimiento.service';
import { ProductService } from '../../core/services/product.service';
import { StockService } from '../../core/services/stock.service';
import { Almacen, Proveedor, Cliente } from '../../core/models/maestro.model';
import { Producto } from '../../core/models/producto.model';
import { Stock } from '../../core/models/stock.model';
import { Entrada, Salida, DetalleMovimiento } from '../../core/models/movimiento.model';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.css'
})
export class MovimientosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private movimientoService = inject(MovimientoService);
  private productService = inject(ProductService);
  private stockService = inject(StockService);

  // Listas maestras
  almacenes: Almacen[] = [];
  proveedores: Proveedor[] = [];
  clientes: Cliente[] = [];
  productos: Producto[] = [];
  
  // Cache de existencias físicas para validación de salidas (anti-corrupción)
  existenciasFisicas: Stock[] = [];

  movimientoForm!: FormGroup;
  tipoSeleccionado: 'ENTRADA' | 'SALIDA' = 'ENTRADA';
  submitting: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  // Mensaje y bandera de error de stock insuficiente por fila
  stockErrors: { [index: number]: string } = {};

  // Variables de Historial
  viewMode: 'registrar' | 'historial' = 'registrar';
  historialMovimientos: any[] = [];
  loadingHistorial: boolean = false;

  ngOnInit(): void {
    this.inicializarFormulario();
    this.cargarCatalogos();
  }

  inicializarFormulario(): void {
    this.movimientoForm = this.fb.group({
      codigoDocumento: ['', [Validators.required, Validators.minLength(4)]],
      tipoMovimiento: ['ENTRADA', [Validators.required]],
      almacenId: ['', [Validators.required]],
      proveedorId: ['', []], // Requerido condicionalmente para ENTRADA
      clienteId: ['', []],   // Requerido condicionalmente para SALIDA
      detalles: this.fb.array([], [Validators.required])
    });

    // Escuchar cambios de tipo
    this.movimientoForm.get('tipoMovimiento')?.valueChanges.subscribe(val => {
      this.tipoSeleccionado = val;
      this.actualizarValidadoresTipo(val);
      this.validarTodosLosStocks();
    });

    // Escuchar cambios en almacén para re-validar stock en salidas
    this.movimientoForm.get('almacenId')?.valueChanges.subscribe(() => {
      this.validarTodosLosStocks();
    });

    // Agregar la primera línea de detalle por defecto
    this.agregarDetalle();
  }

  actualizarValidadoresTipo(tipo: 'ENTRADA' | 'SALIDA'): void {
    const provCtrl = this.movimientoForm.get('proveedorId');
    const clCtrl = this.movimientoForm.get('clienteId');

    if (tipo === 'ENTRADA') {
      provCtrl?.setValidators([Validators.required]);
      clCtrl?.clearValidators();
    } else {
      clCtrl?.setValidators([Validators.required]);
      provCtrl?.clearValidators();
    }
    provCtrl?.updateValueAndValidity();
    clCtrl?.updateValueAndValidity();
  }

  get detalles(): FormArray {
    return this.movimientoForm.get('detalles') as FormArray;
  }

  agregarDetalle(): void {
    const detalleGroup = this.fb.group({
      productoId: ['', [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precioUnitario: [0, [Validators.required, Validators.min(0.01)]]
    });

    // Autocompletar el precio unitario del producto al seleccionarlo
    detalleGroup.get('productoId')?.valueChanges.subscribe(pId => {
      const prod = this.productos.find(p => p.id === Number(pId));
      if (prod) {
        const defaultPrice = this.tipoSeleccionado === 'ENTRADA' ? prod.precioCompra : prod.precioVenta;
        detalleGroup.patchValue({ precioUnitario: defaultPrice });
      }
      this.validarStockFila(this.detalles.length - 1, detalleGroup);
    });

    // Re-validar stock en salidas al cambiar la cantidad
    detalleGroup.get('cantidad')?.valueChanges.subscribe(() => {
      this.validarStockFila(this.detalles.length - 1, detalleGroup);
    });

    this.detalles.push(detalleGroup);
  }

  eliminarDetalle(index: number): void {
    this.detalles.removeAt(index);
    delete this.stockErrors[index];
    this.reordenarErroresStock();
  }

  reordenarErroresStock(): void {
    const nuevosErrores: { [index: number]: string } = {};
    this.detalles.controls.forEach((ctrl, idx) => {
      // Re-validar cada fila para corregir los índices del mapa de errores
      this.validarStockFila(idx, ctrl as FormGroup);
    });
  }

  cargarCatalogos(): void {
    this.movimientoService.listarAlmacenes().subscribe(data => this.almacenes = data);
    this.movimientoService.listarProveedores().subscribe(data => this.proveedores = data);
    this.movimientoService.listarClientes().subscribe(data => this.clientes = data);
    
    this.productService.listar().subscribe(data => {
      this.productos = data;
      // Pre-cargar precios en filas vacías si ya están cargados los productos
      this.detalles.controls.forEach(ctrl => {
        const pId = ctrl.get('productoId')?.value;
        if (pId) {
          const prod = this.productos.find(p => p.id === Number(pId));
          if (prod) {
            const defaultPrice = this.tipoSeleccionado === 'ENTRADA' ? prod.precioCompra : prod.precioVenta;
            ctrl.patchValue({ precioUnitario: defaultPrice });
          }
        }
      });
    });

    // Cargar existencias físicas para la validación anti-corrupción
    this.stockService.listarExistencias().subscribe(data => {
      this.existenciasFisicas = data;
    });
  }

  validarStockFila(index: number, group: FormGroup): void {
    delete this.stockErrors[index];

    if (this.tipoSeleccionado !== 'SALIDA') {
      return;
    }

    const almacenId = this.movimientoForm.get('almacenId')?.value;
    const productoId = group.get('productoId')?.value;
    const cantidad = group.get('cantidad')?.value;

    if (!almacenId || !productoId || !cantidad) {
      return;
    }

    // Buscar en la cache de existencias el stock físico real
    const stockExistente = this.existenciasFisicas.find(s => 
      s.almacen.id === Number(almacenId) && 
      s.producto.id === Number(productoId)
    );

    const stockDisponible = stockExistente ? stockExistente.cantidad : 0;

    if (cantidad > stockDisponible) {
      this.stockErrors[index] = `Stock insuficiente. Disponible: ${stockDisponible} unidades.`;
      group.get('cantidad')?.setErrors({ stockInsuficiente: true });
    } else {
      // Eliminar el error si ya es válido
      const errors = group.get('cantidad')?.errors;
      if (errors) {
        delete errors['stockInsuficiente'];
        if (Object.keys(errors).length === 0) {
          group.get('cantidad')?.setErrors(null);
        } else {
          group.get('cantidad')?.setErrors(errors);
        }
      }
    }
  }

  validarTodosLosStocks(): void {
    this.detalles.controls.forEach((ctrl, idx) => {
      this.validarStockFila(idx, ctrl as FormGroup);
    });
  }

  hasStockErrors(): boolean {
    return Object.keys(this.stockErrors).length > 0;
  }

  getValuacionTotal(): number {
    if (!this.detalles) return 0;
    return this.detalles.controls.reduce((sum, ctrl) => {
      const cant = ctrl.get('cantidad')?.value || 0;
      const precio = ctrl.get('precioUnitario')?.value || 0;
      return sum + (cant * precio);
    }, 0);
  }

  getMovValuation(mov: any): number {
    if (!mov || !mov.detalles) return 0;
    return mov.detalles.reduce((sum: number, det: any) => sum + (det.cantidad * det.precioUnitario), 0);
  }

  onSubmit(): void {
    this.validarTodosLosStocks();
    
    if (this.movimientoForm.invalid || this.hasStockErrors() || this.detalles.length === 0) {
      this.movimientoForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const formVal = this.movimientoForm.value;

    // Resolver Almacen
    const almacen = this.almacenes.find(a => a.id === Number(formVal.almacenId))!;

    // Resolver Detalles
    const detallesBackend: DetalleMovimiento[] = formVal.detalles.map((d: any) => {
      const producto = this.productos.find(p => p.id === Number(d.productoId))!;
      return {
        producto: producto,
        cantidad: Number(d.cantidad),
        precioUnitario: Number(d.precioUnitario)
      };
    });

    if (this.tipoSeleccionado === 'ENTRADA') {
      const proveedor = this.proveedores.find(p => p.id === Number(formVal.proveedorId))!;
      const entrada: Entrada = {
        codigoDocumento: formVal.codigoDocumento,
        tipoMovimiento: 'ENTRADA',
        almacen: almacen,
        proveedor: proveedor,
        detalles: detallesBackend
      };

      this.movimientoService.registrarEntrada(entrada).subscribe({
        next: (res) => {
          this.submitting = false;
          this.successMessage = res;
          this.resetearFormulario();
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = 'Error al registrar entrada en el servidor: ' + (err.error || 'Operación fallida.');
          console.error(err);
        }
      });
    } else {
      const cliente = this.clientes.find(c => c.id === Number(formVal.clienteId))!;
      const salida: Salida = {
        codigoDocumento: formVal.codigoDocumento,
        tipoMovimiento: 'SALIDA',
        almacen: almacen,
        cliente: cliente,
        detalles: detallesBackend
      };

      this.movimientoService.registrarSalida(salida).subscribe({
        next: (res) => {
          this.submitting = false;
          this.successMessage = res;
          this.resetearFormulario();
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = 'Error al registrar salida en el servidor: ' + (err.error || 'Operación fallida.');
          console.error(err);
        }
      });
    }
  }

  resetearFormulario(): void {
    this.movimientoForm.reset({
      tipoMovimiento: this.tipoSeleccionado,
      codigoDocumento: ''
    });
    this.detalles.clear();
    this.agregarDetalle();
    this.stockErrors = {};
    
    // Recargar existencias físicas actualizadas
    this.stockService.listarExistencias().subscribe(data => {
      this.existenciasFisicas = data;
    });

    // Limpiar mensaje de éxito tras unos segundos
    setTimeout(() => {
      this.successMessage = '';
    }, 4000);
  }

  cargarHistorial(): void {
    this.loadingHistorial = true;
    this.movimientoService.listarTodos().subscribe({
      next: (data) => {
        // Ordenar por fecha descendente
        this.historialMovimientos = data.sort((a, b) => {
          if (!a.fecha || !b.fecha) return 0;
          return new Date(b.fecha).getTime() - new Date(a.fecha).getTime();
        });
        this.loadingHistorial = false;
      },
      error: (err) => {
        console.error('Error al cargar historial', err);
        this.loadingHistorial = false;
      }
    });
  }

  cambiarModo(modo: 'registrar' | 'historial'): void {
    this.viewMode = modo;
    if (modo === 'historial') {
      this.cargarHistorial();
    }
  }
}
