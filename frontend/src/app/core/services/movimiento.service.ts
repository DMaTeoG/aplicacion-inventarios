import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Almacen, Proveedor, Cliente, UnidadMedida } from '../models/maestro.model';
import { Categoria } from '../models/producto.model';
import { Entrada, Salida } from '../models/movimiento.model';

@Injectable({
  providedIn: 'root'
})
export class MovimientoService {
  private http = inject(HttpClient);
  private maestrosUrl = '/api/maestros';
  private movimientosUrl = '/api/movimientos';

  // Endpoints Maestros
  listarAlmacenes(): Observable<Almacen[]> {
    return this.http.get<Almacen[]>(`${this.maestrosUrl}/almacenes`);
  }

  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.maestrosUrl}/proveedores`);
  }

  listarClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.maestrosUrl}/clientes`);
  }

  listarUnidades(): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>(`${this.maestrosUrl}/unidades`);
  }

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.maestrosUrl}/categorias`);
  }

  // Endpoints de Transacciones
  registrarEntrada(entrada: Entrada): Observable<string> {
    return this.http.post(`${this.movimientosUrl}/entradas`, entrada, { responseType: 'text' });
  }

  registrarSalida(salida: Salida): Observable<string> {
    return this.http.post(`${this.movimientosUrl}/salidas`, salida, { responseType: 'text' });
  }

  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(this.movimientosUrl);
  }
}
