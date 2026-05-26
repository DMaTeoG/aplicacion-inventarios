package Inventarios.app.repository;

import Inventarios.app.model.ConfiguracionContable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfiguracionContableRepository extends IBaseRepository<ConfiguracionContable, Long> {
    Optional<ConfiguracionContable> findByOperacion(String operacion);
}
