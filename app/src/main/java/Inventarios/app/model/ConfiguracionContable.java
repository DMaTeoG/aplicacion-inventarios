package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "configuraciones_contables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String operacion; // Ej: "ENTRADA_DEBITO", "ENTRADA_CREDITO", "SALIDA_DEBITO_VENTA", etc.
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_contable_id", nullable = false)
    private CuentaContable cuentaContable;
}
