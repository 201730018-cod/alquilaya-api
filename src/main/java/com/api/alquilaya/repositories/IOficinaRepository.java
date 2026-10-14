package com.api.alquilaya.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.alquilaya.models.OficinaModel;

@Repository
public interface IOficinaRepository extends JpaRepository<OficinaModel, Long> {

    List<OficinaModel> findByCiudadIgnoreCase(String ciudad);

    List<OficinaModel> findByRegionIgnoreCase(String region);
}
