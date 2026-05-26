package Inventarios.app.repository;

import Inventarios.app.model.Permiso;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PermisoRepository extends IBaseRepository<Permiso, Long> {
    Optional<Permiso> findByNombre(String nombre);
}
