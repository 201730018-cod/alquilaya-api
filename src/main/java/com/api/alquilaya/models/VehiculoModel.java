package com.api.alquilaya.models;

import java.math.BigDecimal;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "vehiculos")
public class VehiculoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long idVehiculo;

    // Cada vehiculo pertenece a una oficina
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_oficina", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OficinaModel oficina;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 40, message = "La marca no puede exceder 40 caracteres")
    @Column(name = "marca", nullable = false, length = 40)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 40, message = "El modelo no puede exceder 40 caracteres")
    @Column(name = "modelo", nullable = false, length = 40)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private int anio;

    // La matricula es el identificador real del vehiculo, no se repite
    @NotBlank(message = "La matricula es obligatoria")
    @Size(max = 15, message = "La matricula no puede exceder 15 caracteres")
    @Column(name = "matricula", unique = true, nullable = false, length = 15)
    private String matricula;

    @NotNull(message = "Debe indicar el tipo de vehiculo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoVehiculo tipo;

    @NotNull(message = "La tarifa diaria es obligatoria")
    @Column(name = "tarifa_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaDiaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(name = "kilometraje", nullable = false)
    private int kilometraje;

    public boolean estaDisponible() {
        return this.estado == EstadoVehiculo.DISPONIBLE;
    }

    public String getDescripcionCorta() {
        return this.marca + " " + this.modelo + " (" + this.matricula + ")";
    }

    public VehiculoModel() {
    }

    public VehiculoModel(OficinaModel oficina, String marca, String modelo, int anio,
            String matricula, TipoVehiculo tipo, BigDecimal tarifaDiaria) {
        this.oficina = oficina;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.matricula = matricula;
        this.tipo = tipo;
        this.tarifaDiaria = tarifaDiaria;
        this.estado = EstadoVehiculo.DISPONIBLE;
    }

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public OficinaModel getOficina() {
        return oficina;
    }

    public void setOficina(OficinaModel oficina) {
        this.oficina = oficina;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getTarifaDiaria() {
        return tarifaDiaria;
    }

    public void setTarifaDiaria(BigDecimal tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = kilometraje;
    }
}
