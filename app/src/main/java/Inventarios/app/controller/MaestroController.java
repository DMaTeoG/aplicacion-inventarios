package Inventarios.app.controller;

import Inventarios.app.model.*;
import Inventarios.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestros")
public class MaestroController {

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/almacenes")
    public List<Almacen> listarAlmacenes() {
        return almacenRepository.findAll();
    }

    @GetMapping("/proveedores")
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    @GetMapping("/clientes")
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @GetMapping("/unidades")
    public List<UnidadMedida> listarUnidades() {
        return unidadMedidaRepository.findAll();
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }
}
