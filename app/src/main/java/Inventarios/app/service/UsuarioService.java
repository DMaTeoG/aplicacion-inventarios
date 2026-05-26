package Inventarios.app.service;

import Inventarios.app.model.Usuario;
import Inventarios.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean validarCredenciales(String username, String password) {
        System.out.println(">>> Intentando validar credenciales para el usuario: '" + username + "'");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean pwdMatch = usuario.getPassword().equals(password);
            System.out.println(">>> Usuario encontrado en BD. Activo: " + usuario.isActivo() + 
                               " | Contraseña Guardada: '" + usuario.getPassword() + 
                               "' | Contraseña Recibida: '" + password + "' | ¿Coinciden?: " + pwdMatch);
            return pwdMatch && usuario.isActivo();
        } else {
            System.out.println(">>> ERROR: El usuario '" + username + "' NO existe en la base de datos.");
            return false;
        }
    }
}
