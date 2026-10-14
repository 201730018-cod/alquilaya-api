package com.api.alquilaya.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.alquilaya.models.ClienteModel;
import com.api.alquilaya.models.EstadoAlquiler;
import com.api.alquilaya.repositories.IAlquilerRepository;
import com.api.alquilaya.repositories.IClienteRepository;

@Service
public class ClienteService {

    private final IClienteRepository clienteRepository;
    private final IAlquilerRepository alquilerRepository;

    public ClienteService(IClienteRepository clienteRepository,
            IAlquilerRepository alquilerRepository) {
        this.clienteRepository = clienteRepository;
        this.alquilerRepository = alquilerRepository;
    }

    public List<ClienteModel> obtenerClientes() {
        return clienteRepository.findAll();
    }

    public ClienteModel obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un cliente con id: " + id));
    }

    public List<ClienteModel> buscarPorApellido(String apellido) {
        return clienteRepository.findByApellidoContainingIgnoreCase(apellido);
    }

    public ClienteModel guardarCliente(ClienteModel cliente) {
        // Regla: el email no se puede repetir
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con el email: " + cliente.getEmail());
        }
        // Regla: la licencia de conducir no se puede repetir
        if (clienteRepository.existsByLicenciaConducir(cliente.getLicenciaConducir())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con la licencia: " + cliente.getLicenciaConducir());
        }
        cliente.setFechaAlta(null);
        return clienteRepository.save(cliente);
    }

    public ClienteModel actualizarCliente(Long id, ClienteModel datos) {
        ClienteModel cliente = obtenerPorId(id);

        if (!cliente.getEmail().equals(datos.getEmail())
                && clienteRepository.existsByEmail(datos.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con el email: " + datos.getEmail());
        }
        if (!cliente.getLicenciaConducir().equals(datos.getLicenciaConducir())
                && clienteRepository.existsByLicenciaConducir(datos.getLicenciaConducir())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cliente con la licencia: " + datos.getLicenciaConducir());
        }

        cliente.setNombreCliente(datos.getNombreCliente());
        cliente.setApellido(datos.getApellido());
        cliente.setEmail(datos.getEmail());
        cliente.setLicenciaConducir(datos.getLicenciaConducir());
        cliente.setTelefono(datos.getTelefono());

        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id) {
        ClienteModel cliente = obtenerPorId(id);
        // Regla: no se da de baja a un cliente con un vehiculo sin devolver
        long activos = alquilerRepository.contarPorClienteYEstado(id, EstadoAlquiler.ACTIVO);
        if (activos > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el cliente tiene " + activos + " alquiler(es) activo(s)");
        }
        clienteRepository.delete(cliente);
    }
}
