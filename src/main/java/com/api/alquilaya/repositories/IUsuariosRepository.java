package com.api.alquilaya.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


import com.api.alquilaya.models.UsuariosModel;

public interface IUsuariosRepository extends JpaRepository<UsuariosModel, Long> {
    // Aquí puedes definir métodos de consulta personalizados si es necesario      
    
    Optional<UsuariosModel> findByEmail(String email);

    boolean existsByEmail(String email);
    
}
