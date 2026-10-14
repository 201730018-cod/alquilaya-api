package com.api.alquilaya.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.api.alquilaya.models.EstadoVehiculo;
import com.api.alquilaya.models.TipoVehiculo;
import com.api.alquilaya.models.VehiculoModel;
import com.api.alquilaya.services.VehiculoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // GET /api/vehiculos
    // GET /api/vehiculos?estado=DISPONIBLE
    // GET /api/vehiculos?tipo=COCHE
    // GET /api/vehiculos?marca=seat
    // GET /api/vehiculos?oficina=1
    // GET /api/vehiculos?disponibles=true
    @GetMapping
    public List<VehiculoModel> obtenerVehiculos(
            @RequestParam(required = false) EstadoVehiculo estado,
            @RequestParam(required = false) TipoVehiculo tipo,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Long oficina,
            @RequestParam(required = false) Boolean disponibles) {

        if (Boolean.TRUE.equals(disponibles) && oficina != null) {
            return vehiculoService.buscarDisponiblesPorOficina(oficina);
        }
        if (Boolean.TRUE.equals(disponibles)) {
            return vehiculoService.buscarDisponibles();
        }
        if (estado != null) {
            return vehiculoService.buscarPorEstado(estado);
        }
        if (tipo != null) {
            return vehiculoService.buscarPorTipo(tipo);
        }
        if (marca != null) {
            return vehiculoService.buscarPorMarca(marca);
        }
        if (oficina != null) {
            return vehiculoService.buscarPorOficina(oficina);
        }
        return vehiculoService.obtenerVehiculos();
    }

    @GetMapping("/{id}")
    public VehiculoModel obtenerPorId(@PathVariable Long id) {
        return vehiculoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoModel crearVehiculo(@Valid @RequestBody VehiculoModel vehiculo) {
        return vehiculoService.guardarVehiculo(vehiculo);
    }

    @PutMapping("/{id}")
    public VehiculoModel actualizarVehiculo(@PathVariable Long id,
            @Valid @RequestBody VehiculoModel datos) {
        return vehiculoService.actualizarVehiculo(id, datos);
    }

    // PATCH /api/vehiculos/1/estado?valor=MANTENIMIENTO
    @PatchMapping("/{id}/estado")
    public VehiculoModel cambiarEstado(@PathVariable Long id,
            @RequestParam EstadoVehiculo valor) {
        return vehiculoService.cambiarEstado(id, valor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarVehiculo(@PathVariable Long id) {
        vehiculoService.eliminarVehiculo(id);
    }
}
