/* ============================================================
   AlquilaYa S.A. — Frontend
   Cliente de la API REST. Todas las peticiones se autentican
   con HTTP Basic contra los endpoints /api/**
   ============================================================ */

const API = '/api';

// Credenciales de la sesion, en memoria mientras la pagina este abierta
let credenciales = null;
let usuarioActual = null;

// Catalogos que se reutilizan en los formularios
let oficinas = [];
let clientes = [];
let vehiculos = [];


/* ===================== PETICIONES ===================== */

/**
 * Envuelve fetch agregando la cabecera de autenticacion y
 * traduciendo los errores de la API a un mensaje legible.
 */
async function peticion(ruta, opciones = {}) {
    const cabeceras = Object.assign(
        { 'Content-Type': 'application/json' },
        opciones.headers || {}
    );

    if (credenciales) {
        cabeceras['Authorization'] = 'Basic ' + credenciales;
    }

    const respuesta = await fetch(API + ruta, Object.assign({}, opciones, { headers: cabeceras }));

    if (respuesta.status === 204) {
        return null;
    }

    let cuerpo = null;
    const texto = await respuesta.text();
    if (texto) {
        try {
            cuerpo = JSON.parse(texto);
        } catch (e) {
            cuerpo = { message: texto };
        }
    }

    if (!respuesta.ok) {
        const mensaje = (cuerpo && (cuerpo.message || cuerpo.error))
            || 'Error ' + respuesta.status;
        const error = new Error(mensaje);
        error.status = respuesta.status;
        throw error;
    }

    return cuerpo;
}


/* ===================== AVISOS ===================== */

let temporizadorAviso = null;

function avisar(mensaje, tipo = 'exito') {
    const caja = document.getElementById('aviso');
    caja.textContent = mensaje;
    caja.className = 'aviso ' + tipo;
    caja.hidden = false;

    clearTimeout(temporizadorAviso);
    temporizadorAviso = setTimeout(() => { caja.hidden = true; }, 5000);

    window.scrollTo({ top: 0, behavior: 'smooth' });
}


/* ===================== ACCESO ===================== */

document.getElementById('formLogin').addEventListener('submit', async (evento) => {
    evento.preventDefault();

    const usuario = document.getElementById('loginUsuario').value.trim();
    const password = document.getElementById('loginPassword').value;
    const cajaError = document.getElementById('loginError');

    cajaError.hidden = true;
    credenciales = btoa(usuario + ':' + password);

    try {
        // Se valida la credencial contra un endpoint real de la API
        await peticion('/oficinas');

        usuarioActual = usuario;
        document.getElementById('sesionUsuario').textContent = usuario;
        document.getElementById('sesionRol').textContent =
            usuario.startsWith('admin') ? 'Administrador' : 'Operador';

        document.getElementById('pantallaLogin').hidden = true;
        document.getElementById('aplicacion').hidden = false;

        await cargarTodo();
    } catch (error) {
        credenciales = null;
        cajaError.textContent = error.status === 401
            ? 'Correo o contraseña incorrectos.'
            : 'No se pudo conectar con el servidor: ' + error.message;
        cajaError.hidden = false;
    }
});

document.getElementById('btnSalir').addEventListener('click', () => {
    credenciales = null;
    usuarioActual = null;
    document.getElementById('loginPassword').value = '';
    document.getElementById('aplicacion').hidden = true;
    document.getElementById('pantallaLogin').hidden = false;
});


/* ===================== NAVEGACION ===================== */

document.querySelectorAll('.menu-item').forEach((boton) => {
    boton.addEventListener('click', () => {
        document.querySelectorAll('.menu-item').forEach(b => b.classList.remove('activa'));
        document.querySelectorAll('.seccion').forEach(s => s.classList.remove('activa'));

        boton.classList.add('activa');
        document.getElementById('seccion-' + boton.dataset.seccion).classList.add('activa');
    });
});


/* ===================== UTILIDADES ===================== */

function dinero(valor) {
    if (valor === null || valor === undefined) return '—';
    return Number(valor).toLocaleString('es-MX', {
        style: 'currency', currency: 'MXN', minimumFractionDigits: 2
    });
}

function fecha(valor) {
    if (!valor) return '—';
    const d = new Date(valor.length === 10 ? valor + 'T00:00:00' : valor);
    return d.toLocaleDateString('es-MX', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function etiqueta(texto, color) {
    return '<span class="etiqueta ' + color + '">' + texto + '</span>';
}

function colorEstadoVehiculo(estado) {
    if (estado === 'DISPONIBLE') return 'verde';
    if (estado === 'ALQUILADO') return 'azul';
    if (estado === 'MANTENIMIENTO') return 'ambar';
    return 'gris';
}

function colorEstadoAlquiler(estado) {
    if (estado === 'ACTIVO') return 'azul';
    if (estado === 'FINALIZADO') return 'verde';
    return 'gris';
}

function filaVacia(columnas, mensaje) {
    return '<tr class="vacia"><td colspan="' + columnas + '">' + mensaje + '</td></tr>';
}

function diasEntre(inicio, fin) {
    const a = new Date(inicio + 'T00:00:00');
    const b = new Date(fin + 'T00:00:00');
    return Math.round((b - a) / 86400000);
}


/* ===================== CARGA GENERAL ===================== */

async function cargarTodo() {
    await Promise.all([
        cargarOficinas(),
        cargarClientes(),
        cargarVehiculos(),
        cargarAlquileres(),
        cargarMantenimientos()
    ]);
    await cargarPanel();
}


/* ===================== PANEL ===================== */

async function cargarPanel() {
    try {
        const vencidos = await peticion('/alquileres/vencidos');
        const activos = await peticion('/alquileres?estado=ACTIVO');

        document.getElementById('kpiVehiculos').textContent = vehiculos.length;
        document.getElementById('kpiDisponibles').textContent =
            vehiculos.filter(v => v.estado === 'DISPONIBLE').length;
        document.getElementById('kpiActivos').textContent = activos.length;
        document.getElementById('kpiVencidos').textContent = vencidos.length;

        const cuerpo = document.getElementById('tablaVencidos');
        if (!vencidos.length) {
            cuerpo.innerHTML = filaVacia(5, 'No hay devoluciones vencidas.');
            return;
        }

        cuerpo.innerHTML = vencidos.map(a => {
            const retraso = diasEntre(a.fechaDevolucion, new Date().toISOString().slice(0, 10));
            return '<tr>'
                + '<td class="numero">' + a.idAlquiler + '</td>'
                + '<td>' + a.cliente.nombreCliente + ' ' + a.cliente.apellido + '</td>'
                + '<td>' + a.vehiculo.marca + ' ' + a.vehiculo.modelo
                + ' (' + a.vehiculo.matricula + ')</td>'
                + '<td class="numero">' + fecha(a.fechaDevolucion) + '</td>'
                + '<td>' + etiqueta(retraso + ' dias', 'rojo') + '</td>'
                + '</tr>';
        }).join('');
    } catch (error) {
        avisar('No se pudo cargar el panel: ' + error.message, 'error');
    }
}


/* ===================== OFICINAS ===================== */

async function cargarOficinas() {
    try {
        oficinas = await peticion('/oficinas');

        const cuerpo = document.getElementById('tablaOficinas');
        cuerpo.innerHTML = oficinas.length
            ? oficinas.map(o =>
                '<tr>'
                + '<td class="numero">' + o.idOficina + '</td>'
                + '<td>' + o.nombreOficina + '</td>'
                + '<td>' + o.direccion + '</td>'
                + '<td>' + o.ciudad + '</td>'
                + '<td>' + o.region + '</td>'
                + '<td><button class="btn btn-mini peligro" '
                + 'onclick="eliminarOficina(' + o.idOficina + ')">Eliminar</button></td>'
                + '</tr>').join('')
            : filaVacia(6, 'No hay oficinas registradas.');

        const select = document.getElementById('vehOficina');
        select.innerHTML = oficinas.map(o =>
            '<option value="' + o.idOficina + '">' + o.nombreOficina
            + ' — ' + o.ciudad + '</option>').join('');
    } catch (error) {
        avisar('No se pudieron cargar las oficinas: ' + error.message, 'error');
    }
}

document.getElementById('formOficina').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
        await peticion('/oficinas', {
            method: 'POST',
            body: JSON.stringify({
                nombreOficina: document.getElementById('ofiNombre').value.trim(),
                direccion: document.getElementById('ofiDireccion').value.trim(),
                ciudad: document.getElementById('ofiCiudad').value.trim(),
                region: document.getElementById('ofiRegion').value.trim()
            })
        });
        evento.target.reset();
        await cargarOficinas();
        avisar('Oficina registrada correctamente.');
    } catch (error) {
        avisar(error.message, 'error');
    }
});

async function eliminarOficina(id) {
    if (!confirm('Eliminar la oficina ' + id + '?')) return;
    try {
        await peticion('/oficinas/' + id, { method: 'DELETE' });
        await cargarOficinas();
        avisar('Oficina eliminada.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}


/* ===================== CLIENTES ===================== */

async function cargarClientes() {
    try {
        clientes = await peticion('/clientes');

        const cuerpo = document.getElementById('tablaClientes');
        cuerpo.innerHTML = clientes.length
            ? clientes.map(c =>
                '<tr>'
                + '<td class="numero">' + c.idCliente + '</td>'
                + '<td>' + c.nombreCliente + ' ' + c.apellido + '</td>'
                + '<td>' + c.email + '</td>'
                + '<td>' + c.licenciaConducir + '</td>'
                + '<td>' + (c.telefono || '—') + '</td>'
                + '<td class="numero">' + fecha(c.fechaAlta) + '</td>'
                + '<td><button class="btn btn-mini peligro" '
                + 'onclick="eliminarCliente(' + c.idCliente + ')">Eliminar</button></td>'
                + '</tr>').join('')
            : filaVacia(7, 'No hay clientes registrados.');

        const select = document.getElementById('alqCliente');
        select.innerHTML = clientes.map(c =>
            '<option value="' + c.idCliente + '">' + c.nombreCliente + ' ' + c.apellido
            + ' — ' + c.licenciaConducir + '</option>').join('');
    } catch (error) {
        avisar('No se pudieron cargar los clientes: ' + error.message, 'error');
    }
}

document.getElementById('formCliente').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
        await peticion('/clientes', {
            method: 'POST',
            body: JSON.stringify({
                nombreCliente: document.getElementById('cliNombre').value.trim(),
                apellido: document.getElementById('cliApellido').value.trim(),
                email: document.getElementById('cliEmail').value.trim(),
                licenciaConducir: document.getElementById('cliLicencia').value.trim(),
                telefono: document.getElementById('cliTelefono').value.trim()
            })
        });
        evento.target.reset();
        await cargarClientes();
        avisar('Cliente registrado correctamente.');
    } catch (error) {
        avisar(error.message, 'error');
    }
});

async function eliminarCliente(id) {
    if (!confirm('Eliminar el cliente ' + id + '?')) return;
    try {
        await peticion('/clientes/' + id, { method: 'DELETE' });
        await cargarClientes();
        avisar('Cliente eliminado.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}


/* ===================== VEHICULOS ===================== */

async function cargarVehiculos() {
    try {
        const filtro = document.getElementById('filtroVehiculos').value;
        const ruta = filtro ? '/vehiculos?estado=' + filtro : '/vehiculos';

        const lista = await peticion(ruta);
        if (!filtro) {
            vehiculos = lista;
        }

        const cuerpo = document.getElementById('tablaVehiculos');
        cuerpo.innerHTML = lista.length
            ? lista.map(v =>
                '<tr>'
                + '<td class="numero">' + v.idVehiculo + '</td>'
                + '<td><strong>' + v.matricula + '</strong></td>'
                + '<td>' + v.marca + ' ' + v.modelo + ' <span style="color:#6b7a8d">('
                + v.anio + ')</span></td>'
                + '<td>' + v.tipo.replace('_', ' ') + '</td>'
                + '<td>' + (v.oficina ? v.oficina.nombreOficina : '—') + '</td>'
                + '<td class="numero">' + dinero(v.tarifaDiaria) + '</td>'
                + '<td class="numero">' + v.kilometraje.toLocaleString('es-MX') + '</td>'
                + '<td>' + etiqueta(v.estado, colorEstadoVehiculo(v.estado)) + '</td>'
                + '<td style="white-space:nowrap">'
                + (v.estado === 'ALQUILADO' ? ''
                    : '<button class="btn btn-mini" onclick="cambiarEstadoVehiculo('
                      + v.idVehiculo + ', \'' + (v.estado === 'MANTENIMIENTO'
                            ? 'DISPONIBLE' : 'MANTENIMIENTO') + '\')">'
                      + (v.estado === 'MANTENIMIENTO' ? 'Liberar' : 'A taller') + '</button>')
                + '<button class="btn btn-mini peligro" onclick="eliminarVehiculo('
                + v.idVehiculo + ')">Eliminar</button>'
                + '</td>'
                + '</tr>').join('')
            : filaVacia(9, 'No hay vehiculos con ese criterio.');

        // Selectores que dependen de la flota
        const disponibles = vehiculos.filter(v => v.estado === 'DISPONIBLE');
        document.getElementById('alqVehiculo').innerHTML = disponibles.length
            ? disponibles.map(v =>
                '<option value="' + v.idVehiculo + '" data-tarifa="' + v.tarifaDiaria
                + '" data-tipo="' + v.tipo + '">'
                + v.marca + ' ' + v.modelo + ' (' + v.matricula + ') — '
                + dinero(v.tarifaDiaria) + '/dia</option>').join('')
            : '<option value="">No hay vehiculos disponibles</option>';

        document.getElementById('manVehiculo').innerHTML = vehiculos.map(v =>
            '<option value="' + v.idVehiculo + '" data-km="' + v.kilometraje + '">'
            + v.marca + ' ' + v.modelo + ' (' + v.matricula + ')</option>').join('');
    } catch (error) {
        avisar('No se pudieron cargar los vehiculos: ' + error.message, 'error');
    }
}

document.getElementById('filtroVehiculos').addEventListener('change', cargarVehiculos);

document.getElementById('formVehiculo').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
        await peticion('/vehiculos', {
            method: 'POST',
            body: JSON.stringify({
                oficina: { idOficina: Number(document.getElementById('vehOficina').value) },
                tipo: document.getElementById('vehTipo').value,
                marca: document.getElementById('vehMarca').value.trim(),
                modelo: document.getElementById('vehModelo').value.trim(),
                anio: Number(document.getElementById('vehAnio').value),
                matricula: document.getElementById('vehMatricula').value.trim(),
                tarifaDiaria: Number(document.getElementById('vehTarifa').value),
                kilometraje: Number(document.getElementById('vehKm').value)
            })
        });
        evento.target.reset();
        document.getElementById('vehAnio').value = 2024;
        document.getElementById('vehKm').value = 0;
        await cargarVehiculos();
        await cargarPanel();
        avisar('Vehiculo registrado correctamente.');
    } catch (error) {
        avisar(error.message, 'error');
    }
});

async function cambiarEstadoVehiculo(id, estado) {
    try {
        await peticion('/vehiculos/' + id + '/estado?valor=' + estado, { method: 'PATCH' });
        await cargarVehiculos();
        await cargarPanel();
        avisar('Estado del vehiculo actualizado a ' + estado + '.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}

async function eliminarVehiculo(id) {
    if (!confirm('Eliminar el vehiculo ' + id + '?')) return;
    try {
        await peticion('/vehiculos/' + id, { method: 'DELETE' });
        await cargarVehiculos();
        await cargarPanel();
        avisar('Vehiculo eliminado.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}


/* ===================== ALQUILERES ===================== */

async function cargarAlquileres() {
    try {
        const filtro = document.getElementById('filtroAlquileres').value;
        const ruta = filtro ? '/alquileres?estado=' + filtro : '/alquileres';
        const lista = await peticion(ruta);

        const cuerpo = document.getElementById('tablaAlquileres');
        cuerpo.innerHTML = lista.length
            ? lista.map(a => {
                const dias = diasEntre(a.fechaAlquiler, a.fechaDevolucion);
                return '<tr>'
                    + '<td class="numero">' + a.idAlquiler + '</td>'
                    + '<td>' + a.cliente.nombreCliente + ' ' + a.cliente.apellido + '</td>'
                    + '<td>' + a.vehiculo.marca + ' ' + a.vehiculo.modelo
                    + ' <span style="color:#6b7a8d">(' + a.vehiculo.matricula + ')</span></td>'
                    + '<td class="numero">' + fecha(a.fechaAlquiler) + '</td>'
                    + '<td class="numero">' + fecha(a.fechaDevolucion) + '</td>'
                    + '<td class="numero">' + dias + '</td>'
                    + '<td class="numero"><strong>' + dinero(a.precioTotal) + '</strong></td>'
                    + '<td>' + etiqueta(a.estado, colorEstadoAlquiler(a.estado)) + '</td>'
                    + '<td style="white-space:nowrap">'
                    + (a.estado === 'ACTIVO'
                        ? '<button class="btn btn-mini" onclick="devolverAlquiler('
                          + a.idAlquiler + ')">Devolver</button>'
                          + '<button class="btn btn-mini peligro" onclick="cancelarAlquiler('
                          + a.idAlquiler + ')">Cancelar</button>'
                        : '—')
                    + '</td>'
                    + '</tr>';
            }).join('')
            : filaVacia(9, 'No hay alquileres con ese criterio.');
    } catch (error) {
        avisar('No se pudieron cargar los alquileres: ' + error.message, 'error');
    }
}

document.getElementById('filtroAlquileres').addEventListener('change', cargarAlquileres);

// Estimacion del precio mientras se llena el formulario.
// Replica las reglas del servidor; el calculo definitivo lo hace la API.
function estimarPrecio() {
    const select = document.getElementById('alqVehiculo');
    const opcion = select.options[select.selectedIndex];
    const inicio = document.getElementById('alqInicio').value;
    const fin = document.getElementById('alqFin').value;
    const caja = document.getElementById('estimacionPrecio');

    if (!opcion || !opcion.dataset.tarifa || !inicio || !fin) {
        caja.textContent = '';
        return;
    }

    const dias = diasEntre(inicio, fin);
    if (dias <= 0) {
        caja.textContent = 'La fecha de devolucion debe ser posterior a la de alquiler.';
        return;
    }

    const tarifa = Number(opcion.dataset.tarifa);
    const tipo = opcion.dataset.tipo;
    let precio;
    let nota;

    if (tipo === 'COCHE' || tipo === 'COCHE_ELECTRICO') {
        precio = tarifa * dias;
        nota = 'tarifa por dia';
        if (dias >= 7) {
            precio *= 0.90;
            nota += ', con 10% de descuento por larga duracion';
        }
        if (tipo === 'COCHE_ELECTRICO') {
            precio *= 0.85;
            nota += ', mas 15% de descuento ecologico';
        }
    } else if (tipo === 'MOTO') {
        precio = (tarifa + 3) * dias;
        nota = 'tarifa mas 3.00 diarios de seguro obligatorio';
    } else if (tipo === 'FURGONETA') {
        precio = (tarifa + 15) * dias;
        nota = 'tarifa mas 15.00 diarios de recargo por carga';
    } else {
        precio = tarifa * dias;
        nota = 'tarifa por dia';
    }

    caja.textContent = 'Estimado: ' + dinero(precio) + ' por ' + dias
        + ' dia(s) — ' + nota + '.';
}

['alqVehiculo', 'alqInicio', 'alqFin'].forEach(id => {
    document.getElementById(id).addEventListener('change', estimarPrecio);
});

document.getElementById('formAlquiler').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
        await peticion('/alquileres', {
            method: 'POST',
            body: JSON.stringify({
                cliente: { idCliente: Number(document.getElementById('alqCliente').value) },
                vehiculo: { idVehiculo: Number(document.getElementById('alqVehiculo').value) },
                fechaAlquiler: document.getElementById('alqInicio').value,
                fechaDevolucion: document.getElementById('alqFin').value,
                observaciones: document.getElementById('alqObs').value.trim()
            })
        });
        evento.target.reset();
        document.getElementById('estimacionPrecio').textContent = '';
        await cargarVehiculos();
        await cargarAlquileres();
        await cargarPanel();
        avisar('Alquiler registrado correctamente.');
    } catch (error) {
        avisar(error.message, 'error');
    }
});

async function devolverAlquiler(id) {
    const km = prompt('Kilometraje final del vehiculo (dejar vacio para no actualizarlo):');
    if (km === null) return;

    const ruta = km.trim()
        ? '/alquileres/' + id + '/devolver?kmFinal=' + Number(km.trim())
        : '/alquileres/' + id + '/devolver';

    try {
        await peticion(ruta, { method: 'PATCH' });
        await cargarVehiculos();
        await cargarAlquileres();
        await cargarPanel();
        avisar('Devolucion registrada. El vehiculo volvio a estar disponible.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}

async function cancelarAlquiler(id) {
    if (!confirm('Cancelar el alquiler ' + id + '?')) return;
    try {
        await peticion('/alquileres/' + id + '/cancelar', { method: 'PATCH' });
        await cargarVehiculos();
        await cargarAlquileres();
        await cargarPanel();
        avisar('Alquiler cancelado.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}


/* ===================== MANTENIMIENTOS ===================== */

async function cargarMantenimientos() {
    try {
        const lista = await peticion('/mantenimientos');

        const cuerpo = document.getElementById('tablaMantenimientos');
        cuerpo.innerHTML = lista.length
            ? lista.map(m =>
                '<tr>'
                + '<td class="numero">' + m.idMantenimiento + '</td>'
                + '<td>' + m.vehiculo.marca + ' ' + m.vehiculo.modelo
                + ' <span style="color:#6b7a8d">(' + m.vehiculo.matricula + ')</span></td>'
                + '<td class="numero">' + fecha(m.fechaMantenimiento) + '</td>'
                + '<td>' + m.descripcion + '</td>'
                + '<td class="numero">' + m.kilometraje.toLocaleString('es-MX') + '</td>'
                + '<td class="numero">' + dinero(m.costo) + '</td>'
                + '<td><button class="btn btn-mini peligro" onclick="eliminarMantenimiento('
                + m.idMantenimiento + ')">Eliminar</button></td>'
                + '</tr>').join('')
            : filaVacia(7, 'No hay mantenimientos registrados.');
    } catch (error) {
        avisar('No se pudieron cargar los mantenimientos: ' + error.message, 'error');
    }
}

// Al elegir vehiculo se sugiere su kilometraje actual
document.getElementById('manVehiculo').addEventListener('change', (evento) => {
    const opcion = evento.target.options[evento.target.selectedIndex];
    if (opcion && opcion.dataset.km) {
        document.getElementById('manKm').value = opcion.dataset.km;
    }
});

document.getElementById('formMantenimiento').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    try {
        await peticion('/mantenimientos', {
            method: 'POST',
            body: JSON.stringify({
                vehiculo: { idVehiculo: Number(document.getElementById('manVehiculo').value) },
                fechaMantenimiento: document.getElementById('manFecha').value,
                descripcion: document.getElementById('manDescripcion').value.trim(),
                costo: Number(document.getElementById('manCosto').value),
                kilometraje: Number(document.getElementById('manKm').value)
            })
        });
        evento.target.reset();
        await cargarVehiculos();
        await cargarMantenimientos();
        await cargarPanel();
        avisar('Mantenimiento registrado. El vehiculo paso a estado MANTENIMIENTO.');
    } catch (error) {
        avisar(error.message, 'error');
    }
});

async function eliminarMantenimiento(id) {
    if (!confirm('Eliminar el mantenimiento ' + id + '?')) return;
    try {
        await peticion('/mantenimientos/' + id, { method: 'DELETE' });
        await cargarMantenimientos();
        avisar('Mantenimiento eliminado.');
    } catch (error) {
        avisar(error.message, 'error');
    }
}


/* ===================== INICIO ===================== */

// Las fechas de los formularios arrancan en el dia de hoy
const hoy = new Date().toISOString().slice(0, 10);
document.getElementById('alqInicio').value = hoy;
document.getElementById('manFecha').value = hoy;
