package Inventarios.app.repository;

import Inventarios.app.model.Categoria;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends IBaseRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);
}
