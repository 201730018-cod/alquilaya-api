package com.api.alquilaya.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.api.alquilaya.models.MantenimientoModel;
import com.api.alquilaya.services.MantenimientoService;

@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    public MantenimientoController(MantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    // GET /api/mantenimientos
    // GET /api/mantenimientos?vehiculo=1
    // GET /api/mantenimientos?desde=2026-01-01&hasta=2026-12-31
    @GetMapping
    public List<MantenimientoModel> obtenerMantenimientos(
            @RequestParam(required = false) Long vehiculo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        if (vehiculo != null) {
            return mantenimientoService.buscarPorVehiculo(vehiculo);
        }
        if (desde != null && hasta != null) {
            return mantenimientoService.buscarPorPeriodo(desde, hasta);
        }
        return mantenimientoService.obtenerMantenimientos();
    }

    // GET /api/mantenimientos/costo-acumulado/1
    @GetMapping("/costo-acumulado/{idVehiculo}")
    public Map<String, Object> costoAcumulado(@PathVariable Long idVehiculo) {
        BigDecimal total = mantenimientoService.costoAcumulado(idVehiculo);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("idVehiculo", idVehiculo);
        respuesta.put("costoAcumuladoMantenimiento", total);
        return respuesta;
    }

    @GetMapping("/{id}")
    public MantenimientoModel obtenerPorId(@PathVariable Long id) {
        return mantenimientoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MantenimientoModel crearMantenimiento(@RequestBody MantenimientoModel mantenimiento) {
        return mantenimientoService.guardarMantenimiento(mantenimiento);
    }

    @PutMapping("/{id}")
    public MantenimientoModel actualizarMantenimiento(@PathVariable Long id,
            @RequestBody MantenimientoModel datos) {
        return mantenimientoService.actualizarMantenimiento(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMantenimiento(@PathVariable Long id) {
        mantenimientoService.eliminarMantenimiento(id);
    }
}
