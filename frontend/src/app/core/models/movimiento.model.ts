import { Producto } from './producto.model';
import { Almacen, Proveedor, Cliente } from './maestro.model';

export interface DetalleMovimiento {
  id?: number;
  producto: Producto;
  cantidad: number;
  precioUnitario: number;
}

export interface Movimiento {
  id?: number;
  fecha?: string;
  codigoDocumento: string;
  tipoMovimiento: string;
  almacen: Almacen;
  detalles: DetalleMovimiento[];
}

export interface Entrada extends Movimiento {
  proveedor: Proveedor;
}

export interface Salida extends Movimiento {
  cliente: Cliente;
}
