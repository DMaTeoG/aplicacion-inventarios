export interface Almacen {
  id?: number;
  nombre: string;
  ubicacion: string;
}

export interface Persona {
  id?: number;
  nombre: string;
  identificacion: string;
  direccion: string;
  telefono: string;
}

export interface Proveedor extends Persona {
  contactoProveedor?: string;
}

export interface Cliente extends Persona {
  calificacionCredito?: string;
}

export interface UnidadMedida {
  id?: number;
  nombre: string;
  abreviatura: string;
}
