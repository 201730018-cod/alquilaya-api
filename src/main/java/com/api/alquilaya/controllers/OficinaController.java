package com.api.alquilaya.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.api.alquilaya.models.OficinaModel;
import com.api.alquilaya.services.OficinaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/oficinas")
public class OficinaController {

    private final OficinaService oficinaService;

    public OficinaController(OficinaService oficinaService) {
        this.oficinaService = oficinaService;
    }

    @GetMapping
    public List<OficinaModel> obtenerOficinas(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String region) {

        if (ciudad != null) {
            return oficinaService.buscarPorCiudad(ciudad);
        }
        if (region != null) {
            return oficinaService.buscarPorRegion(region);
        }
        return oficinaService.obtenerOficinas();
    }

    @GetMapping("/{id}")
    public OficinaModel obtenerPorId(@PathVariable Long id) {
        return oficinaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OficinaModel crearOficina(@Valid @RequestBody OficinaModel oficina) {
        return oficinaService.guardarOficina(oficina);
    }

    @PutMapping("/{id}")
    public OficinaModel actualizarOficina(@PathVariable Long id,
            @Valid @RequestBody OficinaModel datos) {
        return oficinaService.actualizarOficina(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarOficina(@PathVariable Long id) {
        oficinaService.eliminarOficina(id);
    }
}
