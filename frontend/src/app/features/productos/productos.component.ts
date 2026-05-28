import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { MovimientoService } from '../../core/services/movimiento.service';
import { Producto, Categoria } from '../../core/models/producto.model';
import { UnidadMedida } from '../../core/models/maestro.model';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './productos.component.html',
  styleUrl: './productos.component.css'
})
export class ProductosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private movimientoService = inject(MovimientoService);

  productos: Producto[] = [];
  categorias: Categoria[] = [];
  unidades: UnidadMedida[] = [];

  loading: boolean = false;
  submitting: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  showCreateModal: boolean = false;

  productForm: FormGroup = this.fb.group({
    sku: ['', [Validators.required]],
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    precioCompra: [0, [Validators.required, Validators.min(0.01)]],
    precioVenta: [0, [Validators.required, Validators.min(0.01)]],
    stockMinimo: [5, [Validators.required, Validators.min(1)]],
    categoriaId: ['', [Validators.required]],
    unidadMedidaId: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading = true;
    
    // Cargar Catálogo de Productos
    this.productService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar productos', err);
        this.loading = false;
      }
    });

    // Cargar Categorías
    this.movimientoService.listarCategorias().subscribe({
      next: (data) => this.categorias = data,
      error: (err) => console.error(err)
    });

    // Cargar Unidades
    this.movimientoService.listarUnidades().subscribe({
      next: (data) => this.unidades = data,
      error: (err) => console.error(err)
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const formVal = this.productForm.value;
    
    // Armar el objeto Producto a enviar, resolviendo relaciones
    const nuevoProducto: Producto = {
      sku: formVal.sku,
      nombre: formVal.nombre,
      precioCompra: formVal.precioCompra,
      precioVenta: formVal.precioVenta,
      stockMinimo: Number(formVal.stockMinimo),
      categoria: this.categorias.find(c => c.id === Number(formVal.categoriaId)),
      unidadMedida: this.unidades.find(u => u.id === Number(formVal.unidadMedidaId))
    };

    this.productService.crear(nuevoProducto).subscribe({
      next: (guardado) => {
        this.submitting = false;
        this.successMessage = `Producto '${guardado.nombre}' creado con éxito.`;
        this.productForm.reset({ precioCompra: 0, precioVenta: 0, stockMinimo: 5 });
        this.cargarDatos();
        setTimeout(() => {
          this.showCreateModal = false;
          this.successMessage = '';
        }, 1500);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = 'Ocurrió un error al registrar el producto en el servidor.';
        console.error(err);
      }
    });
  }

  toggleModal(val: boolean): void {
    this.showCreateModal = val;
    if (!val) {
      this.productForm.reset({ precioCompra: 0, precioVenta: 0, stockMinimo: 5 });
      this.errorMessage = '';
    }
  }
}
