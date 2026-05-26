package Inventarios.app.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ReporteContable extends Reporte {
    
    @Override
    public void generar() {
        // Lógica para generar reporte contable (Balance General, Estado de Resultados, etc.)
    }
}
