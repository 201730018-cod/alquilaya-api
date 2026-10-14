package com.api.alquilaya.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.alquilaya.models.EstadoVehiculo;
import com.api.alquilaya.models.TipoVehiculo;
import com.api.alquilaya.models.VehiculoModel;

@Repository
public interface IVehiculoRepository extends JpaRepository<VehiculoModel, Long> {

    boolean existsByMatricula(String matricula);

    List<VehiculoModel> findByEstado(EstadoVehiculo estado);

    List<VehiculoModel> findByTipo(TipoVehiculo tipo);

    List<VehiculoModel> findByMarcaContainingIgnoreCase(String marca);

    @Query("SELECT v FROM VehiculoModel v WHERE v.oficina.idOficina = :idOficina")
    List<VehiculoModel> buscarPorOficina(@Param("idOficina") Long idOficina);

    @Query("SELECT v FROM VehiculoModel v "
         + "WHERE v.estado = com.api.alquilaya.models.EstadoVehiculo.DISPONIBLE "
         + "AND v.oficina.idOficina = :idOficina")
    List<VehiculoModel> buscarDisponiblesPorOficina(@Param("idOficina") Long idOficina);
}
