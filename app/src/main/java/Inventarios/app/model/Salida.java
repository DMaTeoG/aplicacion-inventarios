package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "salidas")
@PrimaryKeyJoinColumn(name = "movimiento_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Salida extends Movimiento {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Override
    public void procesar() {
        // En una Salida (Venta), se disminuye el stock de cada producto en el almacén correspondiente
        if (getDetalles() != null) {
            for (DetalleMovimiento detalle : getDetalles()) {
                detalle.calcularSubtotal();
            }
        }
    }
}
