package com.api.alquilaya.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.alquilaya.models.EstadoVehiculo;
import com.api.alquilaya.models.MantenimientoModel;
import com.api.alquilaya.models.VehiculoModel;
import com.api.alquilaya.repositories.IMantenimientoRepository;
import com.api.alquilaya.repositories.IVehiculoRepository;

@Service
public class MantenimientoService {

    private final IMantenimientoRepository mantenimientoRepository;
    private final IVehiculoRepository vehiculoRepository;

    public MantenimientoService(IMantenimientoRepository mantenimientoRepository,
            IVehiculoRepository vehiculoRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<MantenimientoModel> obtenerMantenimientos() {
        return mantenimientoRepository.findAll();
    }

    public MantenimientoModel obtenerPorId(Long id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un mantenimiento con id: " + id));
    }

    // Historial de servicios de un vehiculo, del mas reciente al mas antiguo
    public List<MantenimientoModel> buscarPorVehiculo(Long idVehiculo) {
        return mantenimientoRepository.buscarPorVehiculo(idVehiculo);
    }

    public List<MantenimientoModel> buscarPorPeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El periodo indicado no es valido");
        }
        return mantenimientoRepository.findByFechaMantenimientoBetween(inicio, fin);
    }

    // Cuanto se ha gastado en mantener un vehiculo
    public BigDecimal costoAcumulado(Long idVehiculo) {
        BigDecimal total = mantenimientoRepository.sumarCostoPorVehiculo(idVehiculo);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional
    public MantenimientoModel guardarMantenimiento(MantenimientoModel mantenimiento) {

        if (mantenimiento.getVehiculo() == null
                || mantenimiento.getVehiculo().getIdVehiculo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el vehiculo");
        }

        Long idVehiculo = mantenimiento.getVehiculo().getIdVehiculo();
        VehiculoModel vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un vehiculo con id: " + idVehiculo));

        // Regla: no se puede mandar a taller un vehiculo que anda alquilado
        if (vehiculo.getEstado() == EstadoVehiculo.ALQUILADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El vehiculo esta alquilado. Registre su devolucion antes del mantenimiento.");
        }

        if (mantenimiento.getCosto() == null
                || mantenimiento.getCosto().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El costo no puede ser negativo");
        }

        // Regla: el kilometraje de un servicio no puede ser menor al del vehiculo
        if (mantenimiento.getKilometraje() < vehiculo.getKilometraje()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El kilometraje del servicio (" + mantenimiento.getKilometraje()
                            + ") no puede ser menor al del vehiculo (" + vehiculo.getKilometraje() + ")");
        }

        mantenimiento.setVehiculo(vehiculo);

        // Al registrar un servicio el vehiculo entra a taller y se actualiza su kilometraje
        vehiculo.setKilometraje(mantenimiento.getKilometraje());
        vehiculo.setEstado(EstadoVehiculo.MANTENIMIENTO);
        vehiculoRepository.save(vehiculo);

        return mantenimientoRepository.save(mantenimiento);
    }

    public MantenimientoModel actualizarMantenimiento(Long id, MantenimientoModel datos) {
        MantenimientoModel mantenimiento = obtenerPorId(id);

        if (datos.getDescripcion() != null && !datos.getDescripcion().isBlank()) {
            mantenimiento.setDescripcion(datos.getDescripcion());
        }
        if (datos.getCosto() != null && datos.getCosto().compareTo(BigDecimal.ZERO) >= 0) {
            mantenimiento.setCosto(datos.getCosto());
        }
        if (datos.getFechaMantenimiento() != null) {
            mantenimiento.setFechaMantenimiento(datos.getFechaMantenimiento());
        }

        return mantenimientoRepository.save(mantenimiento);
    }

    public void eliminarMantenimiento(Long id) {
        MantenimientoModel mantenimiento = obtenerPorId(id);
        mantenimientoRepository.delete(mantenimiento);
    }
}
