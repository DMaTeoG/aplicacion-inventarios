package Inventarios.app.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ReporteInventario extends Reporte {
    
    @Override
    public void generar() {
        // Lógica para generar reporte de inventario (Kardex, stock mínimo, etc.)
    }
}
