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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.api.alquilaya.models.AlquilerModel;
import com.api.alquilaya.models.EstadoAlquiler;
import com.api.alquilaya.services.AlquilerService;

@RestController
@RequestMapping("/api/alquileres")
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    // GET /api/alquileres
    // GET /api/alquileres?estado=ACTIVO
    // GET /api/alquileres?cliente=1
    // GET /api/alquileres?vehiculo=1
    @GetMapping
    public List<AlquilerModel> obtenerAlquileres(
            @RequestParam(required = false) EstadoAlquiler estado,
            @RequestParam(required = false) Long cliente,
            @RequestParam(required = false) Long vehiculo) {

        if (estado != null) {
            return alquilerService.buscarPorEstado(estado);
        }
        if (cliente != null) {
            return alquilerService.buscarPorCliente(cliente);
        }
        if (vehiculo != null) {
            return alquilerService.buscarPorVehiculo(vehiculo);
        }
        return alquilerService.obtenerAlquileres();
    }

    // GET /api/alquileres/vencidos -> vehiculos que no se han devuelto a tiempo
    @GetMapping("/vencidos")
    public List<AlquilerModel> obtenerVencidos() {
        return alquilerService.buscarVencidos();
    }

    // GET /api/alquileres/ingresos?desde=2026-10-01&hasta=2026-10-31
    @GetMapping("/ingresos")
    public Map<String, Object> ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        BigDecimal total = alquilerService.calcularIngresos(desde, hasta);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("reporte", "Ingresos por alquileres finalizados");
        respuesta.put("desde", desde);
        respuesta.put("hasta", hasta);
        respuesta.put("totalIngresos", total);
        return respuesta;
    }

    @GetMapping("/{id}")
    public AlquilerModel obtenerPorId(@PathVariable Long id) {
        return alquilerService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlquilerModel crearAlquiler(@RequestBody AlquilerModel alquiler) {
        return alquilerService.registrarAlquiler(alquiler);
    }

    // PATCH /api/alquileres/1/devolver?kmFinal=15300
    @PatchMapping("/{id}/devolver")
    public AlquilerModel devolver(@PathVariable Long id,
            @RequestParam(required = false) Integer kmFinal) {
        return alquilerService.registrarDevolucion(id, kmFinal);
    }

    @PatchMapping("/{id}/cancelar")
    public AlquilerModel cancelar(@PathVariable Long id) {
        return alquilerService.cancelarAlquiler(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAlquiler(@PathVariable Long id) {
        alquilerService.eliminarAlquiler(id);
    }
}
