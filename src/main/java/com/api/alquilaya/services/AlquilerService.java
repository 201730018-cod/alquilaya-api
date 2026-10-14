package com.api.alquilaya.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.alquilaya.models.AlquilerModel;
import com.api.alquilaya.models.ClienteModel;
import com.api.alquilaya.models.EstadoAlquiler;
import com.api.alquilaya.models.EstadoVehiculo;
import com.api.alquilaya.models.OficinaModel;
import com.api.alquilaya.models.TipoVehiculo;
import com.api.alquilaya.models.VehiculoModel;
import com.api.alquilaya.repositories.IAlquilerRepository;
import com.api.alquilaya.repositories.IClienteRepository;
import com.api.alquilaya.repositories.IOficinaRepository;
import com.api.alquilaya.repositories.IVehiculoRepository;

@Service
public class AlquilerService {

    // Cuantos vehiculos puede tener un mismo cliente al mismo tiempo
    private static final int LIMITE_ALQUILERES_ACTIVOS = 2;

    // Reglas de precio por tipo de vehiculo
    private static final int DIAS_PARA_DESCUENTO = 7;
    private static final BigDecimal DESCUENTO_LARGA_DURACION = new BigDecimal("0.10");
    private static final BigDecimal DESCUENTO_ECOLOGICO = new BigDecimal("0.15");
    private static final BigDecimal SEGURO_DIARIO_MOTO = new BigDecimal("3.00");
    private static final BigDecimal RECARGO_DIARIO_FURGONETA = new BigDecimal("15.00");

    private final IAlquilerRepository alquilerRepository;
    private final IVehiculoRepository vehiculoRepository;
    private final IClienteRepository clienteRepository;
    private final IOficinaRepository oficinaRepository;

    public AlquilerService(IAlquilerRepository alquilerRepository,
            IVehiculoRepository vehiculoRepository,
            IClienteRepository clienteRepository,
            IOficinaRepository oficinaRepository) {
        this.alquilerRepository = alquilerRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.oficinaRepository = oficinaRepository;
    }

    public List<AlquilerModel> obtenerAlquileres() {
        return alquilerRepository.findAll();
    }

    public AlquilerModel obtenerPorId(Long id) {
        return alquilerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un alquiler con id: " + id));
    }

    public List<AlquilerModel> buscarPorEstado(EstadoAlquiler estado) {
        return alquilerRepository.findByEstado(estado);
    }

    public List<AlquilerModel> buscarPorCliente(Long idCliente) {
        return alquilerRepository.buscarPorCliente(idCliente);
    }

    public List<AlquilerModel> buscarPorVehiculo(Long idVehiculo) {
        return alquilerRepository.buscarPorVehiculo(idVehiculo);
    }

    // Alquileres vigentes cuya fecha de devolucion ya paso
    public List<AlquilerModel> buscarVencidos() {
        return alquilerRepository.buscarVencidos(LocalDate.now(), EstadoAlquiler.ACTIVO);
    }

    /**
     * Calcula el precio segun el tipo de vehiculo. Cada tipo aplica su
     * propia regla, igual que en la jerarquia de clases de la primera
     * parte del reto.
     */
    public BigDecimal calcularPrecio(TipoVehiculo tipo, BigDecimal tarifaDiaria, long dias) {

        if (dias <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los dias de alquiler deben ser mayores a cero");
        }

        BigDecimal diasBD = BigDecimal.valueOf(dias);
        BigDecimal precio;

        switch (tipo) {
            case COCHE:
                precio = tarifaDiaria.multiply(diasBD);
                if (dias >= DIAS_PARA_DESCUENTO) {
                    precio = precio.multiply(BigDecimal.ONE.subtract(DESCUENTO_LARGA_DURACION));
                }
                break;

            case COCHE_ELECTRICO:
                // Hereda la regla del coche y encima aplica el descuento ecologico
                precio = tarifaDiaria.multiply(diasBD);
                if (dias >= DIAS_PARA_DESCUENTO) {
                    precio = precio.multiply(BigDecimal.ONE.subtract(DESCUENTO_LARGA_DURACION));
                }
                precio = precio.multiply(BigDecimal.ONE.subtract(DESCUENTO_ECOLOGICO));
                break;

            case MOTO:
                // La moto incluye un seguro obligatorio por dia
                precio = tarifaDiaria.add(SEGURO_DIARIO_MOTO).multiply(diasBD);
                break;

            case FURGONETA:
                // La furgoneta lleva un recargo fijo por dia
                precio = tarifaDiaria.add(RECARGO_DIARIO_FURGONETA).multiply(diasBD);
                break;

            case BICICLETA:
            default:
                precio = tarifaDiaria.multiply(diasBD);
                break;
        }

        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public AlquilerModel registrarAlquiler(AlquilerModel alquiler) {

        if (alquiler.getCliente() == null || alquiler.getCliente().getIdCliente() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el cliente");
        }
        if (alquiler.getVehiculo() == null || alquiler.getVehiculo().getIdVehiculo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el vehiculo");
        }

        Long idCliente = alquiler.getCliente().getIdCliente();
        Long idVehiculo = alquiler.getVehiculo().getIdVehiculo();

        ClienteModel cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un cliente con id: " + idCliente));

        VehiculoModel vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un vehiculo con id: " + idVehiculo));

        // ---- Fechas ----
        LocalDate inicio = alquiler.getFechaAlquiler();
        LocalDate fin = alquiler.getFechaDevolucion();

        if (inicio == null || fin == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar la fecha de alquiler y la de devolucion");
        }
        if (!fin.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de devolucion debe ser posterior a la de alquiler");
        }

        // ---- Regla: el vehiculo debe estar disponible ----
        if (!vehiculo.estaDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El vehiculo esta en estado " + vehiculo.getEstado()
                            + " y no se puede alquilar");
        }

        // ---- Regla: no puede haber otro alquiler vigente en esas fechas ----
        List<AlquilerModel> traslapes = alquilerRepository.buscarTraslapes(
                idVehiculo, inicio, fin, EstadoAlquiler.ACTIVO);
        if (!traslapes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El vehiculo ya tiene un alquiler que se traslapa con esas fechas");
        }

        // ---- Regla: limite de alquileres activos por cliente ----
        long activos = alquilerRepository.contarPorClienteYEstado(idCliente, EstadoAlquiler.ACTIVO);
        if (activos >= LIMITE_ALQUILERES_ACTIVOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cliente ya tiene " + activos + " alquileres activos (limite: "
                            + LIMITE_ALQUILERES_ACTIVOS + ")");
        }

        // ---- Oficina de retorno, opcional ----
        if (alquiler.getOficinaRetorno() != null
                && alquiler.getOficinaRetorno().getIdOficina() != null) {
            Long idOficina = alquiler.getOficinaRetorno().getIdOficina();
            OficinaModel oficina = oficinaRepository.findById(idOficina)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No existe una oficina con id: " + idOficina));
            alquiler.setOficinaRetorno(oficina);
        } else {
            alquiler.setOficinaRetorno(vehiculo.getOficina());
        }

        // ---- Calculo del precio segun el tipo de vehiculo ----
        long dias = ChronoUnit.DAYS.between(inicio, fin);
        BigDecimal precio = calcularPrecio(vehiculo.getTipo(), vehiculo.getTarifaDiaria(), dias);

        // El vehiculo queda ocupado
        vehiculo.setEstado(EstadoVehiculo.ALQUILADO);
        vehiculoRepository.save(vehiculo);

        alquiler.setCliente(cliente);
        alquiler.setVehiculo(vehiculo);
        alquiler.setPrecioTotal(precio);
        alquiler.setEstado(EstadoAlquiler.ACTIVO);
        alquiler.setFechaDevolucionReal(null);
        if (alquiler.getKmInicial() == null) {
            alquiler.setKmInicial(vehiculo.getKilometraje());
        }

        return alquilerRepository.save(alquiler);
    }

    /**
     * Cierra el alquiler: libera el vehiculo, registra la fecha real de
     * entrega y actualiza el kilometraje.
     */
    @Transactional
    public AlquilerModel registrarDevolucion(Long id, Integer kmFinal) {
        AlquilerModel alquiler = obtenerPorId(id);

        if (alquiler.getEstado() != EstadoAlquiler.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede devolver un alquiler ACTIVO. Estado actual: "
                            + alquiler.getEstado());
        }

        VehiculoModel vehiculo = alquiler.getVehiculo();

        if (kmFinal != null) {
            if (kmFinal < vehiculo.getKilometraje()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El kilometraje final no puede ser menor al que tenia el vehiculo");
            }
            alquiler.setKmFinal(kmFinal);
            vehiculo.setKilometraje(kmFinal);
        }

        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        // Si el alquiler tenia oficina de retorno distinta, el vehiculo cambia de sede
        if (alquiler.getOficinaRetorno() != null) {
            vehiculo.setOficina(alquiler.getOficinaRetorno());
        }
        vehiculoRepository.save(vehiculo);

        alquiler.setFechaDevolucionReal(LocalDate.now());
        alquiler.setEstado(EstadoAlquiler.FINALIZADO);

        return alquilerRepository.save(alquiler);
    }

    @Transactional
    public AlquilerModel cancelarAlquiler(Long id) {
        AlquilerModel alquiler = obtenerPorId(id);

        if (alquiler.getEstado() == EstadoAlquiler.FINALIZADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cancelar un alquiler ya finalizado");
        }
        if (alquiler.getEstado() == EstadoAlquiler.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El alquiler ya estaba cancelado");
        }

        VehiculoModel vehiculo = alquiler.getVehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoRepository.save(vehiculo);

        alquiler.setEstado(EstadoAlquiler.CANCELADO);
        return alquilerRepository.save(alquiler);
    }

    @Transactional
    public void eliminarAlquiler(Long id) {
        AlquilerModel alquiler = obtenerPorId(id);
        if (alquiler.getEstado() == EstadoAlquiler.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un alquiler activo. Registre la devolucion primero.");
        }
        alquilerRepository.delete(alquiler);
    }

    public BigDecimal calcularIngresos(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El periodo indicado no es valido");
        }
        BigDecimal total = alquilerRepository.sumarIngresos(inicio, fin, EstadoAlquiler.FINALIZADO);
        return total == null ? BigDecimal.ZERO : total.setScale(2, RoundingMode.HALF_UP);
    }
}
