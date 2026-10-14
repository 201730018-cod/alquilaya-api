package com.api.alquilaya.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.api.alquilaya.repositories.IUsuariosRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Carga el usuario desde la base de datos. Spring Security se encarga
     * de comparar la contrasena contra el hash almacenado.
     */
    @Bean
    public UserDetailsService userDetailsService(IUsuariosRepository usuariosRepository) {
        return email -> usuariosRepository.findByEmail(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getPassword())
                        .roles(usuario.getRol())
                        .disabled(!usuario.isActivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF se desactiva solo para la API REST: es un servicio sin estado
                // que se autentica en cada peticion, no con una sesion de formulario.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // El frontend es publico; lo que esta protegido es la API
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**",
                                         "/favicon.ico", "/login").permitAll()

                        // Dar de baja recursos queda reservado al administrador
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/**")
                            .hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/operador/**").hasAnyRole("ADMIN", "OPERADOR")

                        // El resto de la API: cualquier usuario autenticado
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated())

                // HTTP Basic para que la API se pueda consumir desde Postman
                // y desde el frontend con fetch()
                .httpBasic(basic -> {
                })

                .formLogin(form -> form.permitAll())

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
