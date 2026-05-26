package Inventarios.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nombre;
    
    // Relación recursiva autoreferenciada (Se ignora en la serialización JSON para evitar recursión infinita)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_padre_id")
    @JsonIgnore
    private Categoria padre;
    
    // Subcategorías hijas (Se ignora en la serialización JSON para evitar recursión infinita)
    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Categoria> subcategorias;
}
