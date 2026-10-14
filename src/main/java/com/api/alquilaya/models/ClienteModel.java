package com.api.alquilaya.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "clientes")
public class ClienteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "nombre_cliente", nullable = false, length = 50)
    private String nombreCliente;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    @Column(name = "email", unique = true, nullable = false, length = 80)
    private String email;

    // La licencia es un dato real del cliente y no se puede repetir
    @NotBlank(message = "La licencia de conducir es obligatoria")
    @Size(max = 25, message = "La licencia no puede exceder 25 caracteres")
    @Column(name = "licencia_conducir", unique = true, nullable = false, length = 25)
    private String licenciaConducir;

    @Size(max = 15, message = "El telefono no puede exceder 15 caracteres")
    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDate fechaAlta;

    @PrePersist
    public void onPrePersist() {
        if (this.fechaAlta == null) {
            this.fechaAlta = LocalDate.now();
        }
    }

    public String getNombreCompleto() {
        return this.nombreCliente + " " + this.apellido;
    }

    public ClienteModel() {
    }

    public ClienteModel(String nombreCliente, String apellido, String email,
            String licenciaConducir, String telefono) {
        this.nombreCliente = nombreCliente;
        this.apellido = apellido;
        this.email = email;
        this.licenciaConducir = licenciaConducir;
        this.telefono = telefono;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLicenciaConducir() {
        return licenciaConducir;
    }

    public void setLicenciaConducir(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}
