package Inventarios.app.repository;

import Inventarios.app.model.Movimiento;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepository extends IBaseRepository<Movimiento, Long> {
}
