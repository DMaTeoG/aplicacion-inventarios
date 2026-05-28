import { UnidadMedida } from './maestro.model';

export interface Categoria {
  id?: number;
  nombre: string;
  descripcion: string;
  padre?: Categoria;
  subcategorias?: Categoria[];
}

export interface Producto {
  id?: number;
  sku: string;
  nombre: string;
  precioCompra: number;
  precioVenta: number;
  stockMinimo?: number;
  categoria?: Categoria;
  unidadMedida?: UnidadMedida;
}
