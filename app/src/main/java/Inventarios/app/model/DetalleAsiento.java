package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "detalle_asientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAsiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private double debe;
    private double haber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_contable_id", nullable = false)
    private CuentaContable cuentaContable;
    
    // Relación bidireccional (Se ignora en la serialización JSON para evitar recursión infinita)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_contable_id", nullable = false)
    @JsonIgnore
    private AsientoContable asientoContable;
}
