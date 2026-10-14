package com.api.alquilaya.models;

/**
 * Tipos de vehiculo de la flota. Cada tipo cobra distinto;
 * la regla de precio vive en AlquilerService.
 */
public enum TipoVehiculo {
    COCHE,
    COCHE_ELECTRICO,
    MOTO,
    FURGONETA,
    BICICLETA
}
