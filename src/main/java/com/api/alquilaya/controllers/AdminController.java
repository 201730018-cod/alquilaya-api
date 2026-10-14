package com.api.alquilaya.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController 
@RequestMapping("/api/admin")
public class AdminController {
    
    @GetMapping("/prueba")
    public String prueba() {
        return "Accesos autorizado";
    }
    
}
