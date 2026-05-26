package Inventarios.app.repository;

import Inventarios.app.model.Stock;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockRepository extends IBaseRepository<Stock, Long> {
    Optional<Stock> findByProductoIdAndAlmacenId(Long productoId, Long almacenId);
}
