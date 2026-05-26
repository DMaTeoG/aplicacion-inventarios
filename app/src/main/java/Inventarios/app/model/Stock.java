package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"producto_id", "almacen_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int cantidad;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;
    
    public void aumentar(int cant) {
        this.cantidad += cant;
    }
    
    public void disminuir(int cant) {
        if (this.cantidad < cant) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }
        this.cantidad -= cant;
    }
    
    // Métodos para cumplir con la firma del diagrama sin parámetros (por defecto aumentan/disminuyen en 1)
    public void aumentar() {
        this.aumentar(1);
    }
    
    public void disminuir() {
        this.disminuir(1);
    }
}
