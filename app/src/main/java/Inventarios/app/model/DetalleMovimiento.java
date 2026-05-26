package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "detalle_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer cantidad; // Uso de Integer en lugar de int
    private Double precioUnitario; // Uso de Double en lugar de double para soportar nulos de Jackson
    private Double subtotal; // Uso de Double en lugar de double para soportar nulos de Jackson
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    // Relación bidireccional (Se ignora en la serialización JSON para evitar recursión infinita)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_id", nullable = false)
    @JsonIgnore
    private Movimiento movimiento;
    
    public void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        } else {
            this.subtotal = 0.0;
        }
    }
}
