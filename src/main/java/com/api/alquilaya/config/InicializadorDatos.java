package com.api.alquilaya.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import com.api.alquilaya.repositories.IUsuariosRepository;
import com.api.alquilaya.services.UsuariosService;

@Configuration
public class InicializadorDatos {
    
    @Bean 
    public CommandLineRunner crearUsuarios(
        IUsuariosRepository usuariosRepository,
        UsuariosService usuariosService,
        @Value("${app.admin.email}") String email,
        @Value("${app.admin.password}") String password,
        @Value("${app.operador.email}") String operadorEmail,
        @Value("${app.operador.password}") String operadorPassword) {
            
        return args -> {
            // Crear usuario administrador
            if (!usuariosRepository.existsByEmail(email)) {
                usuariosService.crearUsuario(email, password, "ADMIN");
            }

            // Crear usuario operador
            if (!usuariosRepository.existsByEmail(operadorEmail)) {
                usuariosService.crearUsuario(operadorEmail, operadorPassword, "OPERADOR");
            }
        };
    }
}
