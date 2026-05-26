package Inventarios.app.service;

import Inventarios.app.model.Producto;
import Inventarios.app.model.Stock;
import Inventarios.app.repository.ProductoRepository;
import Inventarios.app.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    public int verificarStock(Long productoId) {
        // En una implementación real, podemos buscar todos los stocks asociados a este producto
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .filter(s -> s.getProducto().getId().equals(productoId))
                .mapToInt(Stock::getCantidad)
                .sum();
    }

    @Transactional
    public void actualizarPreciosGlobales(double porcentaje) {
        List<Producto> productos = productoRepository.findAll();
        for (Producto producto : productos) {
            double nuevoPrecioVenta = producto.getPrecioVenta() * (1 + (porcentaje / 100));
            producto.actualizarPrecio(nuevoPrecioVenta);
            productoRepository.save(producto);
        }
    }
}
