package com.api.alquilaya.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "oficinas")
public class OficinaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oficina")
    private Long idOficina;

    @Column(name = "nombre_oficina", nullable = false, length = 80)
    private String nombreOficina;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "ciudad", nullable = false, length = 60)
    private String ciudad;

    @Column(name = "region", nullable = false, length = 60)
    private String region;

    // Constructor vacio (obligatorio para JPA y Jackson)
    public OficinaModel() {
    }

    // Constructor completo
    public OficinaModel(Long idOficina, String nombreOficina, String direccion, String ciudad, String region) {
        this.idOficina = idOficina;
        this.nombreOficina = nombreOficina;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.region = region;
    }

    // Constructor para registrar una oficina nueva
    public OficinaModel(String nombreOficina, String direccion, String ciudad, String region) {
        this.nombreOficina = nombreOficina;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.region = region;
    }

    // Getters and Setters
    public Long getIdOficina() {
        return idOficina;
    }

    public void setIdOficina(Long idOficina) {
        this.idOficina = idOficina;
    }

    public String getNombreOficina() {
        return nombreOficina;
    }

    public void setNombreOficina(String nombreOficina) {
        this.nombreOficina = nombreOficina;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}