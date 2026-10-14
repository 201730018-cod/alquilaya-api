package com.api.alquilaya.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.alquilaya.models.AlquilerModel;
import com.api.alquilaya.models.EstadoAlquiler;

@Repository
public interface IAlquilerRepository extends JpaRepository<AlquilerModel, Long> {

    List<AlquilerModel> findByEstado(EstadoAlquiler estado);

    @Query("SELECT a FROM AlquilerModel a WHERE a.cliente.idCliente = :idCliente")
    List<AlquilerModel> buscarPorCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT a FROM AlquilerModel a WHERE a.vehiculo.idVehiculo = :idVehiculo")
    List<AlquilerModel> buscarPorVehiculo(@Param("idVehiculo") Long idVehiculo);

    @Query("SELECT COUNT(a) FROM AlquilerModel a "
         + "WHERE a.cliente.idCliente = :idCliente AND a.estado = :estado")
    long contarPorClienteYEstado(@Param("idCliente") Long idCliente,
                                 @Param("estado") EstadoAlquiler estado);

    // Alquileres vigentes del mismo vehiculo que chocan con un rango de fechas
    @Query("SELECT a FROM AlquilerModel a "
         + "WHERE a.vehiculo.idVehiculo = :idVehiculo "
         + "AND a.estado = :activo "
         + "AND a.fechaAlquiler < :fechaDevolucion "
         + "AND a.fechaDevolucion > :fechaAlquiler")
    List<AlquilerModel> buscarTraslapes(@Param("idVehiculo") Long idVehiculo,
                                        @Param("fechaAlquiler") LocalDate fechaAlquiler,
                                        @Param("fechaDevolucion") LocalDate fechaDevolucion,
                                        @Param("activo") EstadoAlquiler activo);

    // Alquileres que no se han devuelto y cuya fecha pactada ya paso
    @Query("SELECT a FROM AlquilerModel a "
         + "WHERE a.estado = :activo AND a.fechaDevolucion < :hoy")
    List<AlquilerModel> buscarVencidos(@Param("hoy") LocalDate hoy,
                                       @Param("activo") EstadoAlquiler activo);

    // Ingresos de los alquileres finalizados dentro de un periodo
    @Query("SELECT COALESCE(SUM(a.precioTotal), 0) FROM AlquilerModel a "
         + "WHERE a.estado = :finalizado "
         + "AND a.fechaAlquiler BETWEEN :inicio AND :fin")
    BigDecimal sumarIngresos(@Param("inicio") LocalDate inicio,
                             @Param("fin") LocalDate fin,
                             @Param("finalizado") EstadoAlquiler finalizado);
}
