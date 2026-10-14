package com.api.alquilaya.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController 
@RequestMapping("/api/operador")
public class OperadorController {
    
    @GetMapping("/prueba")
    public String prueba() {
        return "Accesos autorizado";
    }
    
}
