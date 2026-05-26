package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cuentas_contables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String codigo;
    
    @Column(nullable = false)
    private String nombre;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCuenta tipo;
    
    private double saldo;
    
    public void debitar(double monto) {
        // Regla contable: Activos y Gastos aumentan con Débito; Pasivo, Patrimonio e Ingresos disminuyen con Débito.
        if (tipo == TipoCuenta.ACTIVO || tipo == TipoCuenta.GASTO) {
            this.saldo += monto;
        } else {
            this.saldo -= monto;
        }
    }
    
    public void acreditar(double monto) {
        // Regla contable: Pasivo, Patrimonio e Ingresos aumentan con Crédito; Activos y Gastos disminuyen con Crédito.
        if (tipo == TipoCuenta.PASIVO || tipo == TipoCuenta.PATRIMONIO || tipo == TipoCuenta.INGRESO) {
            this.saldo += monto;
        } else {
            this.saldo -= monto;
        }
    }
    
    // Firmas vacías especificadas en el diagrama
    public void debitar() {
        this.debitar(0.0);
    }
    
    public void acreditar() {
        this.acreditar(0.0);
    }
}
