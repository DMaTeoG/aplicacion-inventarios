package Inventarios.app.controller;

import Inventarios.app.model.Usuario;
import Inventarios.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> getUsers() {
        return usuarioRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario loginReq) {
        // En una implementación real, llamaría a UsuarioService
        // Retornamos un mensaje de simulación
        return ResponseEntity.ok("Simulación de Login exitosa para usuario: " + loginReq.getUsername());
    }
}
