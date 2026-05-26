package Inventarios.app.repository;

import Inventarios.app.model.Producto;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductoRepository extends IBaseRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
}
