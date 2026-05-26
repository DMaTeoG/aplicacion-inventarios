package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "asientos_contables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsientoContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime fecha;
    private String descripcion;
    
    @OneToMany(mappedBy = "asientoContable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleAsiento> detalles;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
    
    public boolean validarPartidaDoble() {
        if (detalles == null || detalles.isEmpty()) {
            return false;
        }
        double sumDebe = detalles.stream().mapToDouble(DetalleAsiento::getDebe).sum();
        double sumHaber = detalles.stream().mapToDouble(DetalleAsiento::getHaber).sum();
        
        // Tolerancia a pequeños errores de redondeo de punto flotante
        return Math.abs(sumDebe - sumHaber) < 0.001;
    }
}
