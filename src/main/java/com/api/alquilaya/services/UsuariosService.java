package com.api.alquilaya.services;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.api.alquilaya.models.UsuariosModel;
import com.api.alquilaya.repositories.IUsuariosRepository;

@Service 
public class UsuariosService {
    private final IUsuariosRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosService(IUsuariosRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuariosModel crearUsuario(String email, String password, String rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        
        UsuariosModel usuario = new UsuariosModel();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public Optional<UsuariosModel> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
