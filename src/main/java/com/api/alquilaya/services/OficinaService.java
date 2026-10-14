package com.api.alquilaya.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.alquilaya.models.OficinaModel;
import com.api.alquilaya.repositories.IOficinaRepository;

@Service
public class OficinaService {

    private final IOficinaRepository oficinaRepository;

    public OficinaService(IOficinaRepository oficinaRepository) {
        this.oficinaRepository = oficinaRepository;
    }

    public List<OficinaModel> obtenerOficinas() {
        return oficinaRepository.findAll();
    }

    public OficinaModel obtenerPorId(Long id) {
        return oficinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe una oficina con id: " + id));
    }

    public List<OficinaModel> buscarPorCiudad(String ciudad) {
        return oficinaRepository.findByCiudadIgnoreCase(ciudad);
    }

    public List<OficinaModel> buscarPorRegion(String region) {
        return oficinaRepository.findByRegionIgnoreCase(region);
    }

    public OficinaModel guardarOficina(OficinaModel oficina) {
        return oficinaRepository.save(oficina);
    }

    public OficinaModel actualizarOficina(Long id, OficinaModel datos) {
        OficinaModel oficina = obtenerPorId(id);
        oficina.setNombreOficina(datos.getNombreOficina());
        oficina.setDireccion(datos.getDireccion());
        oficina.setCiudad(datos.getCiudad());
        oficina.setRegion(datos.getRegion());
        return oficinaRepository.save(oficina);
    }

    public void eliminarOficina(Long id) {
        OficinaModel oficina = obtenerPorId(id);
        oficinaRepository.delete(oficina);
    }
}
