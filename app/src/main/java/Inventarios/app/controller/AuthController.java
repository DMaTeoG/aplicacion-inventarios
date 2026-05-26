package Inventarios.app.controller;

import Inventarios.app.security.JwtUtil;
import Inventarios.app.service.UsuarioService;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Valida las credenciales contra la base de datos de Render y genera un token JWT REAL firmado digitalmente.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<TokenResponse> authenticate(@RequestBody AuthCredentials credentials) {
        boolean esValido = usuarioService.validarCredenciales(credentials.getUsername(), credentials.getPassword());
        
        if (esValido) {
            // Genera el token JWT cifrado y firmado
            String tokenGenerado = jwtUtil.generarToken(credentials.getUsername());
            return ResponseEntity.ok(new TokenResponse(tokenGenerado));
        } else {
            // Retorna un código 401 si las credenciales son inválidas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Clases estáticas auxiliares para la petición y respuesta de autenticación
    @Getter
    @Setter
    public static class AuthCredentials {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TokenResponse {
        private String token;
    }
}
