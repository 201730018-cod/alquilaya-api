package com.api.alquilaya.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.alquilaya.models.EstadoVehiculo;
import com.api.alquilaya.models.OficinaModel;
import com.api.alquilaya.models.TipoVehiculo;
import com.api.alquilaya.models.VehiculoModel;
import com.api.alquilaya.repositories.IOficinaRepository;
import com.api.alquilaya.repositories.IVehiculoRepository;

@Service
public class VehiculoService {

    private final IVehiculoRepository vehiculoRepository;
    private final IOficinaRepository oficinaRepository;

    public VehiculoService(IVehiculoRepository vehiculoRepository,
            IOficinaRepository oficinaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.oficinaRepository = oficinaRepository;
    }

    public List<VehiculoModel> obtenerVehiculos() {
        return vehiculoRepository.findAll();
    }

    public VehiculoModel obtenerPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un vehiculo con id: " + id));
    }

    public List<VehiculoModel> buscarPorEstado(EstadoVehiculo estado) {
        return vehiculoRepository.findByEstado(estado);
    }

    public List<VehiculoModel> buscarPorTipo(TipoVehiculo tipo) {
        return vehiculoRepository.findByTipo(tipo);
    }

    public List<VehiculoModel> buscarPorMarca(String marca) {
        return vehiculoRepository.findByMarcaContainingIgnoreCase(marca);
    }

    public List<VehiculoModel> buscarPorOficina(Long idOficina) {
        return vehiculoRepository.buscarPorOficina(idOficina);
    }

    public List<VehiculoModel> buscarDisponibles() {
        return vehiculoRepository.findByEstado(EstadoVehiculo.DISPONIBLE);
    }

    public List<VehiculoModel> buscarDisponiblesPorOficina(Long idOficina) {
        return vehiculoRepository.buscarDisponiblesPorOficina(idOficina);
    }

    public VehiculoModel guardarVehiculo(VehiculoModel vehiculo) {

        if (vehiculo.getOficina() == null || vehiculo.getOficina().getIdOficina() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar la oficina a la que pertenece el vehiculo");
        }

        Long idOficina = vehiculo.getOficina().getIdOficina();
        OficinaModel oficina = oficinaRepository.findById(idOficina)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe una oficina con id: " + idOficina));

        // Regla: la matricula identifica al vehiculo y no se puede repetir
        if (vehiculoRepository.existsByMatricula(vehiculo.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un vehiculo con la matricula: " + vehiculo.getMatricula());
        }

        if (vehiculo.getTarifaDiaria() == null
                || vehiculo.getTarifaDiaria().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La tarifa diaria debe ser mayor a cero");
        }

        vehiculo.setOficina(oficina);
        if (vehiculo.getEstado() == null) {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        }

        return vehiculoRepository.save(vehiculo);
    }

    public VehiculoModel actualizarVehiculo(Long id, VehiculoModel datos) {
        VehiculoModel vehiculo = obtenerPorId(id);

        if (!vehiculo.getMatricula().equals(datos.getMatricula())
                && vehiculoRepository.existsByMatricula(datos.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un vehiculo con la matricula: " + datos.getMatricula());
        }

        vehiculo.setMarca(datos.getMarca());
        vehiculo.setModelo(datos.getModelo());
        vehiculo.setAnio(datos.getAnio());
        vehiculo.setMatricula(datos.getMatricula());
        vehiculo.setTarifaDiaria(datos.getTarifaDiaria());
        vehiculo.setKilometraje(datos.getKilometraje());
        if (datos.getTipo() != null) {
            vehiculo.setTipo(datos.getTipo());
        }
        if (datos.getOficina() != null && datos.getOficina().getIdOficina() != null) {
            OficinaModel oficina = oficinaRepository.findById(datos.getOficina().getIdOficina())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe una oficina con id: " + datos.getOficina().getIdOficina()));
            vehiculo.setOficina(oficina);
        }

        return vehiculoRepository.save(vehiculo);
    }

    /**
     * Cambia el estado del vehiculo. No permite liberar ni mandar a
     * mantenimiento un vehiculo que esta alquilado: primero hay que
     * cerrar el alquiler.
     */
    public VehiculoModel cambiarEstado(Long id, EstadoVehiculo estado) {
        VehiculoModel vehiculo = obtenerPorId(id);

        if (vehiculo.getEstado() == EstadoVehiculo.ALQUILADO
                && estado != EstadoVehiculo.ALQUILADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El vehiculo esta alquilado. Primero debe registrarse su devolucion.");
        }

        vehiculo.setEstado(estado);
        return vehiculoRepository.save(vehiculo);
    }

    public void eliminarVehiculo(Long id) {
        VehiculoModel vehiculo = obtenerPorId(id);
        if (vehiculo.getEstado() == EstadoVehiculo.ALQUILADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un vehiculo que esta alquilado");
        }
        vehiculoRepository.delete(vehiculo);
    }
}
