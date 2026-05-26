package Inventarios.app.repository;

import Inventarios.app.model.Cliente;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends IBaseRepository<Cliente, Long> {
}
