package Inventarios.app.repository;

import Inventarios.app.model.UnidadMedida;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UnidadMedidaRepository extends IBaseRepository<UnidadMedida, Long> {
    Optional<UnidadMedida> findByAbreviatura(String abreviatura);
}
