package com.api.alquilaya.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "alquileres")
public class AlquilerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alquiler")
    private Long idAlquiler;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehiculoModel vehiculo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ClienteModel cliente;

    // Oficina donde se entrega el vehiculo al terminar.
    // Si va en null se asume que se devuelve en la misma oficina de origen.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_oficina_retorno")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OficinaModel oficinaRetorno;

    // Fechas pactadas al contratar
    @Column(name = "fecha_alquiler", nullable = false)
    private LocalDate fechaAlquiler;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDate fechaDevolucion;

    // Fecha real en que se entrego el vehiculo, se llena al cerrar el alquiler
    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAlquiler estado = EstadoAlquiler.ACTIVO;

    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Column(name = "km_final")
    private Integer kmFinal;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @PrePersist
    public void onPrePersist() {
        if (this.estado == null) {
            this.estado = EstadoAlquiler.ACTIVO;
        }
    }

    // Dias pactados del alquiler
    public long getDiasContratados() {
        if (this.fechaAlquiler == null || this.fechaDevolucion == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(this.fechaAlquiler, this.fechaDevolucion);
    }

    // Dias de retraso si el vehiculo se entrego despues de lo pactado
    public long getDiasDeRetraso() {
        if (this.fechaDevolucionReal == null || this.fechaDevolucion == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(this.fechaDevolucion, this.fechaDevolucionReal);
        return dias > 0 ? dias : 0;
    }

    public AlquilerModel() {
    }

    public AlquilerModel(VehiculoModel vehiculo, ClienteModel cliente,
            LocalDate fechaAlquiler, LocalDate fechaDevolucion) {
        this.vehiculo = vehiculo;
        this.cliente = cliente;
        this.fechaAlquiler = fechaAlquiler;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = EstadoAlquiler.ACTIVO;
    }

    public Long getIdAlquiler() {
        return idAlquiler;
    }

    public void setIdAlquiler(Long idAlquiler) {
        this.idAlquiler = idAlquiler;
    }

    public VehiculoModel getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoModel vehiculo) {
        this.vehiculo = vehiculo;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public OficinaModel getOficinaRetorno() {
        return oficinaRetorno;
    }

    public void setOficinaRetorno(OficinaModel oficinaRetorno) {
        this.oficinaRetorno = oficinaRetorno;
    }

    public LocalDate getFechaAlquiler() {
        return fechaAlquiler;
    }

    public void setFechaAlquiler(LocalDate fechaAlquiler) {
        this.fechaAlquiler = fechaAlquiler;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public EstadoAlquiler getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlquiler estado) {
        this.estado = estado;
    }

    public Integer getKmInicial() {
        return kmInicial;
    }

    public void setKmInicial(Integer kmInicial) {
        this.kmInicial = kmInicial;
    }

    public Integer getKmFinal() {
        return kmFinal;
    }

    public void setKmFinal(Integer kmFinal) {
        this.kmFinal = kmFinal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
