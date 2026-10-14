package com.api.alquilaya.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.alquilaya.models.MantenimientoModel;

@Repository
public interface IMantenimientoRepository extends JpaRepository<MantenimientoModel, Long> {

    @Query("SELECT m FROM MantenimientoModel m WHERE m.vehiculo.idVehiculo = :idVehiculo "
         + "ORDER BY m.fechaMantenimiento DESC")
    List<MantenimientoModel> buscarPorVehiculo(@Param("idVehiculo") Long idVehiculo);

    List<MantenimientoModel> findByFechaMantenimientoBetween(LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(SUM(m.costo), 0) FROM MantenimientoModel m "
         + "WHERE m.vehiculo.idVehiculo = :idVehiculo")
    BigDecimal sumarCostoPorVehiculo(@Param("idVehiculo") Long idVehiculo);
}
