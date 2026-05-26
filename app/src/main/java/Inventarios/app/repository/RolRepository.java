package Inventarios.app.repository;

import Inventarios.app.model.Rol;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends IBaseRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
