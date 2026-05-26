package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "entradas")
@PrimaryKeyJoinColumn(name = "movimiento_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entrada extends Movimiento {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
    
    @Override
    public void procesar() {
        // En una Entrada (Compra), se incrementa el stock de cada producto en el almacén correspondiente
        if (getDetalles() != null) {
            for (DetalleMovimiento detalle : getDetalles()) {
                detalle.calcularSubtotal();
            }
        }
    }
}
