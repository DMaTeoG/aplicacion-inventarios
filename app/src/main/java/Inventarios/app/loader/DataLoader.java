package Inventarios.app.loader;

import Inventarios.app.model.*;
import Inventarios.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import Inventarios.app.service.MovimientoService;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CuentaContableRepository cuentaRepository;

    @Autowired
    private ConfiguracionContableRepository configuracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 0. Crear Rol y Usuario Admin de forma idempotente (sólo si no existen)
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ADMINISTRADOR", null)));

        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("12345"); // Credencial solicitada por el usuario
            admin.setEmail("admin@inventario.com");
            admin.setActivo(true);
            admin.setRoles(new java.util.HashSet<>(java.util.Arrays.asList(rolAdmin)));
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario 'admin' creado exitosamente con contraseña '12345'.");
        }

        // 0.1 Crear Proveedor y Cliente por defecto si no existen
        if (proveedorRepository.count() == 0) {
            Proveedor prov = new Proveedor();
            prov.setNombre("Proveedor Tecnológico Global S.A.");
            prov.setTelefono("+1 800-555-0199");
            prov.setDireccion("Silicon Valley, CA, USA");
            proveedorRepository.save(prov);
            System.out.println(">>> Proveedor por defecto creado.");
        }

        if (clienteRepository.count() == 0) {
            Cliente cl = new Cliente();
            cl.setNombre("Cliente de Prueba General");
            cl.setTelefono("+57 300-123-4567");
            cl.setDireccion("Calle 100 #15-30, Bogotá");
            clienteRepository.save(cl);
            System.out.println(">>> Cliente por defecto creado.");
        }

        // Doble verificación: Si ya existen productos o cuentas, la base de datos ya está sembrada.
        // El early return inicial previene procesamiento innecesario en cada encendido.
        if (productoRepository.count() > 0) {
            System.out.println(">>> Base de datos ya cuenta con datos de prueba de inventario cargados.");
            return;
        }

        System.out.println(">>> Iniciando el sembrado de datos reales de tecnología y catálogo contable...");

        // 1. Crear Cuentas Contables de forma idempotente
        CuentaContable cuentaCaja = obtenerOCrearCuenta("1110", "Caja y Bancos", TipoCuenta.ACTIVO);
        CuentaContable cuentaInventario = obtenerOCrearCuenta("1150", "Inventario de Mercancías", TipoCuenta.ACTIVO);
        CuentaContable cuentaProveedores = obtenerOCrearCuenta("2110", "Proveedores Nacionales", TipoCuenta.PASIVO);
        CuentaContable cuentaVentas = obtenerOCrearCuenta("4110", "Ingresos por Ventas", TipoCuenta.INGRESO);
        CuentaContable cuentaCostoVentas = obtenerOCrearCuenta("5110", "Costo de Ventas", TipoCuenta.GASTO);

        // Guardar configuraciones de mapeo de forma idempotente (sólo si no existen)
        guardarConfiguracionSiNoExiste("ENTRADA_DEBITO", cuentaInventario);
        guardarConfiguracionSiNoExiste("ENTRADA_CREDITO", cuentaProveedores);
        guardarConfiguracionSiNoExiste("SALIDA_DEBITO_CAJA", cuentaCaja);
        guardarConfiguracionSiNoExiste("SALIDA_CREDITO_VENTAS", cuentaVentas);
        guardarConfiguracionSiNoExiste("SALIDA_DEBITO_COSTO", cuentaCostoVentas);
        guardarConfiguracionSiNoExiste("SALIDA_CREDITO_INVENTARIO", cuentaInventario);

        // 2. Crear Unidad de Medida de forma idempotente
        UnidadMedida und = unidadMedidaRepository.findByAbreviatura("UND")
                .orElseGet(() -> unidadMedidaRepository.save(new UnidadMedida(null, "Unidades", "UND")));

        // 3. Crear Jerarquía de Categorías de Tecnología
        Categoria tecnologia = categoriaRepository.findByNombre("Tecnología")
                .orElseGet(() -> categoriaRepository.save(new Categoria(null, "Tecnología", null, new ArrayList<>())));

        Categoria computadoras = categoriaRepository.findByNombre("Laptops y Computación")
                .orElseGet(() -> categoriaRepository.save(new Categoria(null, "Laptops y Computación", tecnologia, new ArrayList<>())));

        Categoria smartphones = categoriaRepository.findByNombre("Smartphones y Celulares")
                .orElseGet(() -> categoriaRepository.save(new Categoria(null, "Smartphones y Celulares", tecnologia, new ArrayList<>())));

        Categoria perifericos = categoriaRepository.findByNombre("Accesorios y Periféricos")
                .orElseGet(() -> categoriaRepository.save(new Categoria(null, "Accesorios y Periféricos", tecnologia, new ArrayList<>())));

        // 4. Crear Almacenes Físicos
        Almacen central = almacenRepository.findByNombre("Almacén Central Tecnológico")
                .orElseGet(() -> almacenRepository.save(new Almacen(null, "Almacén Central Tecnológico", "Sótano Principal - Módulo A")));

        Almacen exhibicion = almacenRepository.findByNombre("Bodega de Exhibición")
                .orElseGet(() -> almacenRepository.save(new Almacen(null, "Bodega de Exhibición", "Pasillo Principal 3 - Stand B")));

        // 5. Crear Productos de Tecnología
        Producto macbook = productoRepository.findBySku("TECH-MBP-01")
                .orElseGet(() -> productoRepository.save(new Producto(null, "TECH-MBP-01", "Laptop Apple MacBook Pro M3 Max 16\"", 2499.00, 3199.00, 5, computadoras, und)));

        Producto iphone = productoRepository.findBySku("TECH-IPH-15")
                .orElseGet(() -> productoRepository.save(new Producto(null, "TECH-IPH-15", "Smartphone Apple iPhone 15 Pro Max 256GB", 899.00, 1199.00, 10, smartphones, und)));

        Producto teclado = productoRepository.findBySku("TECH-LOG-KB")
                .orElseGet(() -> productoRepository.save(new Producto(null, "TECH-LOG-KB", "Teclado Mecánico Logitech MX Mechanical Mini", 79.90, 119.90, 15, perifericos, und)));

        Producto monitor = productoRepository.findBySku("TECH-ASU-MN")
                .orElseGet(() -> productoRepository.save(new Producto(null, "TECH-ASU-MN", "Monitor Gamer Asus ROG Strix 32\" 4K", 449.00, 599.00, 8, perifericos, und)));

        // 6. Asignar existencias iniciales (Stock) por Almacén de forma idempotente
        guardarStockSiNoExiste(15, macbook, central);
        guardarStockSiNoExiste(3, macbook, exhibicion);

        guardarStockSiNoExiste(45, iphone, central);
        guardarStockSiNoExiste(8, iphone, exhibicion);

        guardarStockSiNoExiste(120, teclado, central);
        guardarStockSiNoExiste(25, teclado, exhibicion);

        guardarStockSiNoExiste(30, monitor, central);

        // 7. Crear movimientos de demostración si no existen para dar historial de transacciones de inmediato
        if (movimientoRepository.count() == 0) {
            System.out.println(">>> Sembrando movimientos de demostración...");
            try {
                // Entrada
                Entrada ent = new Entrada();
                ent.setCodigoDocumento("FAC-2026-COMPRA-01");
                ent.setFecha(LocalDateTime.now().minusDays(2));
                ent.setObservacion("Abastecimiento inicial de laptops de alta gama");
                ent.setProveedor(proveedorRepository.findAll().get(0));
                
                DetalleMovimiento det1 = new DetalleMovimiento(null, 5, 2499.00, 12495.00, macbook, ent);
                ent.setDetalles(new java.util.ArrayList<>(java.util.Arrays.asList(det1)));
                
                movimientoService.ejecutarMovimiento(ent);
                
                // Salida
                Salida sal = new Salida();
                sal.setCodigoDocumento("FAC-2026-VENTA-01");
                sal.setFecha(LocalDateTime.now().minusDays(1));
                sal.setObservacion("Venta corporativa de smartphones de prueba");
                sal.setCliente(clienteRepository.findAll().get(0));
                
                DetalleMovimiento det2 = new DetalleMovimiento(null, 2, 1199.00, 2398.00, iphone, sal);
                sal.setDetalles(new java.util.ArrayList<>(java.util.Arrays.asList(det2)));
                
                movimientoService.ejecutarMovimiento(sal);
                System.out.println(">>> ¡Movimientos de demostración e integración contable sembrados con éxito!");
            } catch (Exception e) {
                System.out.println(">>> Error al sembrar movimientos: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println(">>> ¡Sembrado de datos finalizado con éxito! Todos los productos, almacenes, stocks e integraciones contables han sido cargados de forma limpia.");
    }

    private CuentaContable obtenerOCrearCuenta(String codigo, String nombre, TipoCuenta tipo) {
        return cuentaRepository.findByCodigo(codigo)
                .orElseGet(() -> cuentaRepository.save(new CuentaContable(null, codigo, nombre, tipo, 0.0)));
    }

    private void guardarConfiguracionSiNoExiste(String operacion, CuentaContable cuenta) {
        if (configuracionRepository.findByOperacion(operacion).isEmpty()) {
            configuracionRepository.save(new ConfiguracionContable(null, operacion, cuenta));
        }
    }

    private void guardarStockSiNoExiste(int cantidad, Producto producto, Almacen almacen) {
        if (stockRepository.findByProductoIdAndAlmacenId(producto.getId(), almacen.getId()).isEmpty()) {
            stockRepository.save(new Stock(null, cantidad, producto, almacen));
        }
    }
}
