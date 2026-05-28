import { Producto } from './producto.model';
import { Almacen } from './maestro.model';

export interface Stock {
  id?: number;
  producto: Producto;
  almacen: Almacen;
  cantidad: number;
}
