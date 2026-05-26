package Inventarios.app.repository;

import Inventarios.app.model.Almacen;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends IBaseRepository<Almacen, Long> {
    Optional<Almacen> findByNombre(String nombre);
}
