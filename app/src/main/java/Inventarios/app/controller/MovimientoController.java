package Inventarios.app.controller;

import Inventarios.app.model.Movimiento;
import Inventarios.app.model.Entrada;
import Inventarios.app.model.Salida;
import Inventarios.app.repository.MovimientoRepository;
import Inventarios.app.service.MovimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping
    public List<Movimiento> listarTodos() {
        return movimientoRepository.findAll();
    }

    @PostMapping("/entradas")
    public ResponseEntity<String> registrarEntrada(@RequestBody Entrada entrada) {
        movimientoService.ejecutarMovimiento(entrada);
        return ResponseEntity.ok("Entrada de almacén registrada e integrada contablemente con éxito.");
    }

    @PostMapping("/salidas")
    public ResponseEntity<String> registrarSalida(@RequestBody Salida salida) {
        movimientoService.ejecutarMovimiento(salida);
        return ResponseEntity.ok("Salida de almacén registrada e integrada contablemente con éxito.");
    }
}
