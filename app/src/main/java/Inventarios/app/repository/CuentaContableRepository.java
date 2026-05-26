package Inventarios.app.repository;

import Inventarios.app.model.CuentaContable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CuentaContableRepository extends IBaseRepository<CuentaContable, Long> {
    Optional<CuentaContable> findByCodigo(String codigo);
}
