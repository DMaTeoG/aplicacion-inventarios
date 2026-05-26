package Inventarios.app.service;

import Inventarios.app.model.*;
import Inventarios.app.repository.AsientoContableRepository;
import Inventarios.app.repository.CuentaContableRepository;
import Inventarios.app.repository.ConfiguracionContableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ContabilidadService {

    @Autowired
    private AsientoContableRepository asientoRepository;

    @Autowired
    private CuentaContableRepository cuentaRepository;

    @Autowired
    private ConfiguracionContableRepository configuracionRepository;

    @Transactional
    public void generarAsiento(Movimiento movimiento) {
        if (movimiento == null) return;

        // Calcular el monto total del movimiento sumando subtotales
        double totalMonto = 0;
        if (movimiento.getDetalles() != null) {
            for (DetalleMovimiento det : movimiento.getDetalles()) {
                det.calcularSubtotal();
                totalMonto += det.getSubtotal();
            }
        }

        if (totalMonto <= 0) return;

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDateTime.now());
        asiento.setDetalles(new ArrayList<>());

        // Cargar dinámicamente las configuraciones contables desde la BD
        // Si no existen en la BD, se auto-inicializan con los valores estándar definidos
        CuentaContable cuentaInventario = obtenerCuentaParametrizada("ENTRADA_DEBITO", "1150", "Inventario de Mercancías", TipoCuenta.ACTIVO);
        CuentaContable cuentaProveedores = obtenerCuentaParametrizada("ENTRADA_CREDITO", "2110", "Proveedores Nacionales", TipoCuenta.PASIVO);
        CuentaContable cuentaCaja = obtenerCuentaParametrizada("SALIDA_DEBITO_CAJA", "1110", "Caja y Bancos", TipoCuenta.ACTIVO);
        CuentaContable cuentaVentas = obtenerCuentaParametrizada("SALIDA_CREDITO_VENTAS", "4110", "Ingresos por Ventas", TipoCuenta.INGRESO);
        CuentaContable cuentaCostoVentas = obtenerCuentaParametrizada("SALIDA_DEBITO_COSTO", "5110", "Costo de Ventas", TipoCuenta.GASTO);
        CuentaContable cuentaInventarioSalida = obtenerCuentaParametrizada("SALIDA_CREDITO_INVENTARIO", "1150", "Inventario de Mercancías", TipoCuenta.ACTIVO);

        if (movimiento instanceof Entrada) {
            // Entrada de Almacén (Compra): Debitar Inventario (activo aumenta) y Acreditar Proveedores (pasivo aumenta)
            asiento.setDescripcion("Asiento automático por Entrada de Almacén Nro " + movimiento.getId());

            DetalleAsiento debitoInventario = new DetalleAsiento(null, totalMonto, 0, cuentaInventario, asiento);
            DetalleAsiento creditoProveedores = new DetalleAsiento(null, 0, totalMonto, cuentaProveedores, asiento);

            asiento.getDetalles().add(debitoInventario);
            asiento.getDetalles().add(creditoProveedores);

            // Afectar los saldos de las cuentas
            cuentaInventario.debitar(totalMonto);
            cuentaProveedores.acreditar(totalMonto);

            // Guardar saldos de las cuentas afectadas
            cuentaRepository.save(cuentaInventario);
            cuentaRepository.save(cuentaProveedores);

        } else if (movimiento instanceof Salida) {
            // Salida de Almacén (Venta): 
            // 1. Debitar Caja/Bancos (activo aumenta) y Acreditar Ventas (ingreso aumenta)
            // 2. Debitar Costo de Ventas (gasto aumenta) y Acreditar Inventario (activo disminuye)
            asiento.setDescripcion("Asiento automático por Salida de Almacén Nro " + movimiento.getId());

            double totalVenta = totalMonto; 
            double totalCosto = totalMonto * 0.7; // Simulación: Costo es el 70% del valor de venta

            DetalleAsiento debitoCaja = new DetalleAsiento(null, totalVenta, 0, cuentaCaja, asiento);
            DetalleAsiento creditoVentas = new DetalleAsiento(null, 0, totalVenta, cuentaVentas, asiento);
            DetalleAsiento debitoCosto = new DetalleAsiento(null, totalCosto, 0, cuentaCostoVentas, asiento);
            DetalleAsiento creditoInventario = new DetalleAsiento(null, 0, totalCosto, cuentaInventarioSalida, asiento);

            asiento.getDetalles().add(debitoCaja);
            asiento.getDetalles().add(creditoVentas);
            asiento.getDetalles().add(debitoCosto);
            asiento.getDetalles().add(creditoInventario);

            // Afectar los saldos de las cuentas
            cuentaCaja.debitar(totalVenta);
            cuentaVentas.acreditar(totalVenta);
            cuentaCostoVentas.debitar(totalCosto);
            cuentaInventarioSalida.acreditar(totalCosto);

            // Guardar saldos de las cuentas afectadas
            cuentaRepository.save(cuentaCaja);
            cuentaRepository.save(cuentaVentas);
            cuentaRepository.save(cuentaCostoVentas);
            cuentaRepository.save(cuentaInventarioSalida);
        }

        // Validar partida doble
        if (asiento.validarPartidaDoble()) {
            asientoRepository.save(asiento);
            movimiento.setAsientoContable(asiento);
        } else {
            throw new IllegalStateException("Error de consistencia financiera: El asiento contable no cumple con la partida doble.");
        }
    }

    /**
     * Obtiene una cuenta contable según la configuración cargada en la Base de Datos para una operación dada.
     * Si no existe configuración en BD, la inicializa con el código y nombre por defecto provistos.
     */
    private CuentaContable obtenerCuentaParametrizada(String operacion, String codigoDefecto, String nombreDefecto, TipoCuenta tipoDefecto) {
        return configuracionRepository.findByOperacion(operacion)
                .map(ConfiguracionContable::getCuentaContable)
                .orElseGet(() -> {
                    // Si no existe la configuración, buscamos o creamos la cuenta contable
                    CuentaContable cuenta = cuentaRepository.findByCodigo(codigoDefecto)
                            .orElseGet(() -> {
                                CuentaContable nueva = new CuentaContable(null, codigoDefecto, nombreDefecto, tipoDefecto, 0.0);
                                return cuentaRepository.save(nueva);
                            });
                    
                    // Guardamos la configuración de mapeo contable en la BD
                    ConfiguracionContable config = new ConfiguracionContable(null, operacion, cuenta);
                    configuracionRepository.save(config);
                    
                    return cuenta;
                });
    }
}
