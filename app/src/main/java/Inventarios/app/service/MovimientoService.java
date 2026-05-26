package Inventarios.app.service;

import Inventarios.app.model.*;
import Inventarios.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ContabilidadService contabilidadService;

    @Transactional
    public void ejecutarMovimiento(Movimiento mov) {
        if (mov == null) return;

        // Establecer fecha por defecto si no viene dada
        if (mov.getFecha() == null) {
            mov.setFecha(LocalDateTime.now());
        }

        // Vincular bidireccionalmente cada detalle con su movimiento padre para evitar la restricción NOT NULL de base de datos
        if (mov.getDetalles() != null) {
            for (DetalleMovimiento det : mov.getDetalles()) {
                det.setMovimiento(mov);
            }
        }

        // 1. Procesar el movimiento (cálculo de subtotales)
        mov.procesar();

        // 2. Persistir movimiento inicial para generar ID
        Movimiento movGuardado = movimientoRepository.save(mov);

        // 3. Actualizar físicamente el stock en almacén para cada detalle
        if (movGuardado.getDetalles() != null) {
            for (DetalleMovimiento det : movGuardado.getDetalles()) {
                // Para simplificar, asumimos que se actualiza el stock de un almacén predeterminado o uno asignado.
                // En un sistema real, cada detalle o movimiento se asocia a un Almacén.
                // Simularemos la actualización de stock en un almacén estándar si existe, de lo contrario lo creamos.
                Long almacenId = 1L; // Almacén predeterminado
                
                Optional<Stock> stockOpt = stockRepository.findByProductoIdAndAlmacenId(
                        det.getProducto().getId(), almacenId);
                
                if (stockOpt.isPresent()) {
                    Stock stock = stockOpt.get();
                    if (movGuardado instanceof Entrada) {
                        stock.aumentar(det.getCantidad());
                    } else if (movGuardado instanceof Salida) {
                        stock.disminuir(det.getCantidad());
                    }
                    stockRepository.save(stock);
                } else {
                    // Si no existe stock registrado en ese almacén, se crea
                    if (movGuardado instanceof Entrada) {
                        Almacen almacen = almacenRepository.findById(almacenId)
                                .orElseThrow(() -> new IllegalArgumentException("El almacén con ID " + almacenId + " no existe"));
                        Stock nuevoStock = new Stock(null, det.getCantidad(), det.getProducto(), almacen);
                        stockRepository.save(nuevoStock);
                    } else if (movGuardado instanceof Salida) {
                        throw new IllegalArgumentException("No hay stock registrado para el producto en el almacén");
                    }
                }
            }
        }

        // 4. Integración Contable: Generar el Asiento Contable correspondiente
        contabilidadService.generarAsiento(movGuardado);

        // 5. Guardar movimiento actualizado con su relación a AsientoContable
        movimientoRepository.save(movGuardado);
    }
}
