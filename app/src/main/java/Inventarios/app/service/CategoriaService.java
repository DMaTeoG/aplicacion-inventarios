package Inventarios.app.service;

import Inventarios.app.model.Categoria;
import Inventarios.app.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Algoritmo Recursivo Ascendente:
     * Busca la categoría raíz (ancestro principal) de una categoría dada.
     * Ejemplo: Laptops -> Computadoras -> Electrónica (Raíz)
     * 
     * Caso Base: Si la categoría no tiene padre, ella misma es la raíz.
     * Paso Recursivo: Si tiene padre, resolver recursivamente para el padre.
     */
    @Transactional(readOnly = true)
    public Categoria obtenerCategoriaRaiz(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        // Caso Base: No tiene padre, es la raíz
        if (categoria.getPadre() == null) {
            return categoria;
        }
        // Paso Recursivo: Subir un nivel en la jerarquía
        return obtenerCategoriaRaiz(categoria.getPadre());
    }

    /**
     * Algoritmo Recursivo Descendente:
     * Obtiene una lista de todos los IDs de la categoría dada más todas sus subcategorías anidadas.
     * Es sumamente útil para buscar productos en una categoría "padre" y que incluya automáticamente
     * todos los productos de las subcategorías.
     */
    @Transactional(readOnly = true)
    public List<Long> obtenerIdsDeCategoriaYSubcategorias(Long categoriaId) {
        List<Long> ids = new ArrayList<>();
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
        
        if (categoriaOpt.isPresent()) {
            Categoria categoria = categoriaOpt.get();
            // Agregar el ID actual
            ids.add(categoria.getId());
            // Llamada al método recursivo auxiliar
            agregarSubcategoriasIdsRecursivo(categoria, ids);
        }
        
        return ids;
    }

    /**
     * Método recursivo auxiliar descendente.
     * Caso Base Implícito: Si 'subcategorias' está vacía o es nula, el bucle no ejecuta
     * y la recursión se detiene en esa rama de la jerarquía.
     */
    private void agregarSubcategoriasIdsRecursivo(Categoria actual, List<Long> listaIds) {
        if (actual.getSubcategorias() != null) {
            for (Categoria sub : actual.getSubcategorias()) {
                // Registrar el ID de la subcategoría
                listaIds.add(sub.getId());
                // Paso Recursivo: Recorrer los hijos de esta subcategoría
                agregarSubcategoriasIdsRecursivo(sub, listaIds);
            }
        }
    }

    /**
     * Algoritmo de Visualización de Árbol (Recursivo Descendente):
     * Genera una cadena de texto formateada que representa la jerarquía visual de la categoría.
     */
    @Transactional(readOnly = true)
    public String obtenerArbolCategoriasTexto(Long categoriaId) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
        if (categoriaOpt.isEmpty()) {
            return "Categoría no encontrada";
        }
        
        StringBuilder sb = new StringBuilder();
        generarTextoArbolRecursivo(categoriaOpt.get(), 0, sb);
        return sb.toString();
    }

    private void generarTextoArbolRecursivo(Categoria actual, int nivel, StringBuilder sb) {
        // Crear sangría visual según el nivel de profundidad en el árbol
        String sangria = "  ".repeat(nivel);
        String prefijo = nivel > 0 ? "└── " : "";
        
        sb.append(sangria).append(prefijo).append(actual.getNombre()).append("\n");
        
        // Paso Recursivo: Procesar hijos aumentando el nivel de profundidad
        if (actual.getSubcategorias() != null) {
            for (Categoria sub : actual.getSubcategorias()) {
                generarTextoArbolRecursivo(sub, nivel + 1, sb);
            }
        }
    }
}
