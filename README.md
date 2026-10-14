# API REST — AlquilaYa S.A.

API REST para una empresa de alquiler de vehículos. Administra la flota
distribuida en oficinas, los clientes con licencia vigente, el ciclo completo de
los alquileres y el historial de mantenimiento de cada unidad.

El proyecto incluye además un **frontend web** que consume esta misma API, y un
sistema de **autenticación con roles** (administrador y operador).

Sustituye el sistema de escritorio obsoleto y los procesos manuales de la
empresa, y expone las capacidades del negocio mediante un contrato HTTP + JSON
que puede ser consumido por aplicaciones móviles o por terceros, como agencias
de viajes.

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje base |
| Spring Boot | 4.1.1 | Framework principal |
| Spring Web MVC | — | Exposición de endpoints REST |
| Spring Data JPA | — | Persistencia y acceso a datos |
| Spring Security | — | Autenticación HTTP Basic y control de roles |
| Hibernate | — | Implementación de JPA / ORM |
| Bean Validation | — | Validación de datos de entrada |
| BCrypt | — | Cifrado de contraseñas |
| MySQL | 8.x | Base de datos relacional |
| Maven | 3.9 | Gestión de dependencias y build |
| HTML, CSS y JavaScript | — | Frontend que consume la API |

---

## Arquitectura

```
com.api.alquilaya
├── models/          Entidades JPA (@Entity) y enums del dominio
├── repositories/    Interfaces @Repository que extienden JpaRepository
├── services/        Lógica de negocio (@Service)
├── controllers/     Endpoints REST (@RestController)
└── config/          Seguridad e inicialización de usuarios

src/main/resources/static/
├── index.html       Frontend de una sola página
├── css/estilos.css
└── js/app.js        Cliente de la API con fetch()
```

Cada capa depende únicamente de la inmediata inferior. Los controladores no
acceden a los repositorios y los servicios no conocen HTTP.

El frontend no comparte código con el backend: se comunica exclusivamente por
HTTP contra `/api/**` y recibe JSON. Es, en la práctica, el primer consumidor
del contrato que expone la API.

### Modelo de datos

- **Oficina** — sede donde se resguarda y se entrega la flota.
- **Vehículo** — pertenece a una oficina. Su `tipo` determina cómo se calcula el
  precio del alquiler. La matrícula es única.
- **Cliente** — persona con licencia de conducir. El correo y la licencia son únicos.
- **Alquiler** — relaciona cliente y vehículo. Guarda las fechas pactadas, la
  fecha real de devolución y el kilometraje de entrada y salida.
- **Mantenimiento** — historial de servicios de cada vehículo, con costo y
  kilometraje.
- **Usuario** — credenciales de acceso al sistema, con rol ADMIN u OPERADOR.

### Decisiones de diseño

**El tipo de vehículo determina la regla de precio.** El cálculo replica en la
capa de servicios la jerarquía de clases desarrollada en la primera parte del
reto: cada tipo aplica su propia fórmula. Un coche recibe descuento por larga
duración, un coche eléctrico suma además un descuento ecológico, una moto
incluye seguro diario obligatorio y una furgoneta un recargo por carga.

**El alquiler guarda fechas pactadas y reales por separado.** `fechaDevolucion`
es lo que se acordó al contratar y `fechaDevolucionReal` lo que ocurrió. Esto
permite detectar retrasos y alimentar el reporte de devoluciones vencidas.

**El estado del vehículo se sincroniza con las operaciones.** Al registrar un
alquiler el vehículo pasa a ALQUILADO; al devolverlo vuelve a DISPONIBLE. Al
registrar un mantenimiento pasa a MANTENIMIENTO. Estas operaciones son
transaccionales: si algo falla, no queda un vehículo marcado como ocupado sin
alquiler asociado.

**La matrícula y la licencia son identificadores reales.** No se generan de
forma automática porque son datos que existen fuera del sistema.

**Los importes usan `BigDecimal`.** Con `double` los centavos se pierden por
redondeo en operaciones con dinero.

---

## Seguridad

La API está protegida con Spring Security:

- Las contraseñas se almacenan cifradas con BCrypt, nunca en texto plano.
- Los endpoints `/api/**` requieren autenticación **HTTP Basic**.
- Las operaciones `DELETE` sobre cualquier recurso quedan reservadas al rol
  `ADMIN`.
- Los archivos del frontend son públicos; lo que está protegido es la API.
- CSRF se desactiva únicamente para `/api/**`, por tratarse de un servicio REST
  que se autentica en cada petición.

Al arrancar por primera vez, la clase `InicializadorDatos` crea dos usuarios
tomando los valores de las variables de entorno:

| Rol | Variable de correo | Variable de contraseña |
|---|---|---|
| ADMIN | `ADMIN_EMAIL` | `ADMIN_PASSWORD` |
| OPERADOR | `OPERADOR_EMAIL` | `OPERADOR_PASSWORD` |

---

## Instalación y ejecución

### Requisitos previos

- JDK 21 o superior
- MySQL 8 en ejecución
- Maven (incluido mediante el wrapper `mvnw`)

### 1. Clonar el repositorio

```bash
git clone https://github.com/201730018-cod/alquilaya-api.git
cd alquilaya-api
```

### 2. Crear la base de datos

```sql
CREATE DATABASE alquilaya;
```

Las tablas se generan automáticamente al arrancar.

### 3. Configurar las variables de entorno

Copiar `.env.example` como `.env` y completar los valores.

### 4. Ejecutar

**Windows (CMD):**
```cmd
set DB_PASSWORD=tu_password
set ADMIN_PASSWORD=tu_password_admin
set OPERADOR_PASSWORD=tu_password_operador
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
export DB_PASSWORD=tu_password
export ADMIN_PASSWORD=tu_password_admin
export OPERADOR_PASSWORD=tu_password_operador
./mvnw spring-boot:run
```

### 5. Acceder

- **Frontend:** `http://localhost:8082`
- **API:** `http://localhost:8082/api/...`

Se entra al frontend con el correo y la contraseña del administrador.

---

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `alquilaya` |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | — (obligatoria) |
| `SERVER_PORT` | Puerto de la aplicación | `8082` |
| `ADMIN_EMAIL` | Correo del administrador | `admin@alquilaya.com` |
| `ADMIN_PASSWORD` | Contraseña del administrador | — (obligatoria) |
| `OPERADOR_EMAIL` | Correo del operador | `operador@alquilaya.com` |
| `OPERADOR_PASSWORD` | Contraseña del operador | — (obligatoria) |

Las contraseñas no tienen valor por defecto de forma deliberada: si faltan, la
aplicación no arranca en lugar de usar credenciales previsibles. El archivo
`.env` está excluido mediante `.gitignore` y nunca se versiona.

---

## Endpoints

Todos requieren autenticación HTTP Basic.

### Oficinas — `/api/oficinas`

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/oficinas` | Lista todas las oficinas | 200 |
| GET | `/api/oficinas?ciudad=Veracruz` | Filtra por ciudad | 200 |
| GET | `/api/oficinas?region=Veracruz` | Filtra por región | 200 |
| GET | `/api/oficinas/{id}` | Obtiene una oficina | 200 / 404 |
| POST | `/api/oficinas` | Registra una oficina | 201 / 400 |
| PUT | `/api/oficinas/{id}` | Actualiza una oficina | 200 / 404 |
| DELETE | `/api/oficinas/{id}` | Elimina una oficina | 204 / 403 / 404 |

### Clientes — `/api/clientes`

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/clientes` | Lista todos los clientes | 200 |
| GET | `/api/clientes?apellido=mendez` | Filtra por apellido | 200 |
| GET | `/api/clientes/{id}` | Obtiene un cliente | 200 / 404 |
| POST | `/api/clientes` | Registra un cliente | 201 / 400 / 409 |
| PUT | `/api/clientes/{id}` | Actualiza un cliente | 200 / 404 / 409 |
| DELETE | `/api/clientes/{id}` | Elimina un cliente | 204 / 403 / 404 / 409 |

### Vehículos — `/api/vehiculos`

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/vehiculos` | Lista toda la flota | 200 |
| GET | `/api/vehiculos?estado=DISPONIBLE` | Filtra por estado | 200 |
| GET | `/api/vehiculos?tipo=COCHE` | Filtra por tipo | 200 |
| GET | `/api/vehiculos?marca=tesla` | Filtra por marca | 200 |
| GET | `/api/vehiculos?oficina=1` | Vehículos de una oficina | 200 |
| GET | `/api/vehiculos?disponibles=true` | Solo los disponibles | 200 |
| GET | `/api/vehiculos/{id}` | Obtiene un vehículo | 200 / 404 |
| POST | `/api/vehiculos` | Registra un vehículo | 201 / 400 / 404 / 409 |
| PUT | `/api/vehiculos/{id}` | Actualiza un vehículo | 200 / 404 / 409 |
| PATCH | `/api/vehiculos/{id}/estado?valor=MANTENIMIENTO` | Cambia el estado | 200 / 404 / 409 |
| DELETE | `/api/vehiculos/{id}` | Elimina un vehículo | 204 / 403 / 404 / 409 |

### Alquileres — `/api/alquileres`

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/alquileres` | Lista todos los alquileres | 200 |
| GET | `/api/alquileres?estado=ACTIVO` | Filtra por estado | 200 |
| GET | `/api/alquileres?cliente=1` | Alquileres de un cliente | 200 |
| GET | `/api/alquileres?vehiculo=1` | Alquileres de un vehículo | 200 |
| GET | `/api/alquileres/vencidos` | Devoluciones vencidas | 200 |
| GET | `/api/alquileres/ingresos?desde=&hasta=` | Ingresos del periodo | 200 / 400 |
| GET | `/api/alquileres/{id}` | Obtiene un alquiler | 200 / 404 |
| POST | `/api/alquileres` | Registra un alquiler | 201 / 400 / 404 / 409 |
| PATCH | `/api/alquileres/{id}/devolver?kmFinal=` | Registra la devolución | 200 / 400 / 404 / 409 |
| PATCH | `/api/alquileres/{id}/cancelar` | Cancela el alquiler | 200 / 404 / 409 |
| DELETE | `/api/alquileres/{id}` | Elimina un alquiler | 204 / 403 / 404 / 409 |

### Mantenimientos — `/api/mantenimientos`

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/mantenimientos` | Lista todos los servicios | 200 |
| GET | `/api/mantenimientos?vehiculo=1` | Historial de un vehículo | 200 |
| GET | `/api/mantenimientos?desde=&hasta=` | Servicios de un periodo | 200 / 400 |
| GET | `/api/mantenimientos/costo-acumulado/{id}` | Gasto total de un vehículo | 200 |
| GET | `/api/mantenimientos/{id}` | Obtiene un servicio | 200 / 404 |
| POST | `/api/mantenimientos` | Registra un servicio | 201 / 400 / 404 / 409 |
| PUT | `/api/mantenimientos/{id}` | Actualiza un servicio | 200 / 404 |
| DELETE | `/api/mantenimientos/{id}` | Elimina un servicio | 204 / 403 / 404 |

### Valores de los enums

- `TipoVehiculo`: `COCHE`, `COCHE_ELECTRICO`, `MOTO`, `FURGONETA`, `BICICLETA`
- `EstadoVehiculo`: `DISPONIBLE`, `ALQUILADO`, `MANTENIMIENTO`, `BAJA`
- `EstadoAlquiler`: `ACTIVO`, `FINALIZADO`, `CANCELADO`

---

## Reglas de negocio

**Cálculo del precio según el tipo de vehículo**

| Tipo | Regla |
|---|---|
| Coche | Tarifa × días. Descuento del 10% a partir de 7 días |
| Coche eléctrico | Igual que el coche, más un 15% de descuento ecológico |
| Moto | (Tarifa + 3.00 de seguro diario) × días |
| Furgoneta | (Tarifa + 15.00 de recargo diario) × días |
| Bicicleta | Tarifa × días |

**Vehículos**
- La matrícula es única en toda la flota.
- La tarifa diaria debe ser mayor a cero.
- No se puede liberar ni mandar a taller un vehículo que está alquilado.
- No se puede eliminar un vehículo alquilado.

**Clientes**
- El correo y la licencia de conducir son únicos.
- No se puede eliminar un cliente con alquileres activos.

**Alquileres**
- La fecha de devolución debe ser posterior a la de alquiler.
- El vehículo debe estar disponible.
- No puede existir otro alquiler vigente que se traslape en esas fechas.
- Un cliente puede tener como máximo dos alquileres activos simultáneos.
- Al registrar el alquiler el vehículo pasa a ALQUILADO; al devolverlo vuelve a
  DISPONIBLE y se actualiza su kilometraje.
- No se puede eliminar un alquiler activo.

**Mantenimientos**
- No se puede registrar un servicio de un vehículo que está alquilado.
- El kilometraje del servicio no puede ser menor al que tiene el vehículo.
- El costo no puede ser negativo.

---

## Códigos de respuesta

| Código | Significado | Ejemplo |
|---|---|---|
| 200 OK | Consulta o actualización correcta | Devolver un vehículo |
| 201 Created | Recurso creado | Registrar un alquiler |
| 204 No Content | Eliminación correcta | Eliminar una oficina |
| 400 Bad Request | Datos de entrada inválidos | Fechas incoherentes |
| 401 Unauthorized | Falta autenticación o es incorrecta | Petición sin credenciales |
| 403 Forbidden | El rol no tiene permiso | Operador intentando un DELETE |
| 404 Not Found | El recurso no existe | Vehículo con id inexistente |
| 409 Conflict | Conflicto con el estado actual | Matrícula duplicada |

---

## Ejemplos de peticiones y respuestas

Todas las peticiones incluyen la cabecera de autenticación:

```http
Authorization: Basic YWRtaW5AYWxxdWlsYXlhLmNvbTpBZG1pbjEyMzQ=
```

### Registrar una oficina

```http
POST /api/oficinas
Content-Type: application/json
```
```json
{
  "nombreOficina": "Sucursal Centro",
  "direccion": "Av. Independencia 320",
  "ciudad": "Veracruz",
  "region": "Veracruz"
}
```

**Respuesta — 201 Created**
```json
{
  "idOficina": 1,
  "nombreOficina": "Sucursal Centro",
  "direccion": "Av. Independencia 320",
  "ciudad": "Veracruz",
  "region": "Veracruz"
}
```

### Registrar un vehículo

```http
POST /api/vehiculos
Content-Type: application/json
```
```json
{
  "oficina": { "idOficina": 1 },
  "tipo": "COCHE_ELECTRICO",
  "marca": "Tesla",
  "modelo": "Model 3",
  "anio": 2024,
  "matricula": "VER-1001",
  "tarifaDiaria": 1800.00,
  "kilometraje": 12500
}
```

**Respuesta — 201 Created**
```json
{
  "idVehiculo": 1,
  "matricula": "VER-1001",
  "marca": "Tesla",
  "modelo": "Model 3",
  "tipo": "COCHE_ELECTRICO",
  "tarifaDiaria": 1800.00,
  "estado": "DISPONIBLE",
  "kilometraje": 12500
}
```

### Registrar un cliente

```http
POST /api/clientes
Content-Type: application/json
```
```json
{
  "nombreCliente": "Ana",
  "apellido": "Mendez",
  "email": "ana.mendez@correo.com",
  "licenciaConducir": "LIC-0012345",
  "telefono": "2299887766"
}
```

### Registrar un alquiler

```http
POST /api/alquileres
Content-Type: application/json
```
```json
{
  "cliente": { "idCliente": 1 },
  "vehiculo": { "idVehiculo": 1 },
  "fechaAlquiler": "2026-10-20",
  "fechaDevolucion": "2026-10-28"
}
```

**Respuesta — 201 Created**
```json
{
  "idAlquiler": 1,
  "fechaAlquiler": "2026-10-20",
  "fechaDevolucion": "2026-10-28",
  "fechaDevolucionReal": null,
  "precioTotal": 11016.00,
  "estado": "ACTIVO",
  "diasContratados": 8,
  "diasDeRetraso": 0,
  "kmInicial": 12500
}
```

El precio se calculó solo: 1800.00 × 8 días = 14,400.00, menos 10% por larga
duración = 12,960.00, menos 15% de descuento ecológico = **11,016.00**.

### Registrar la devolución

```http
PATCH /api/alquileres/1/devolver?kmFinal=13200
```

**Respuesta — 200 OK**
```json
{
  "idAlquiler": 1,
  "fechaDevolucionReal": "2026-10-28",
  "estado": "FINALIZADO",
  "kmInicial": 12500,
  "kmFinal": 13200
}
```

El vehículo vuelve automáticamente al estado DISPONIBLE y su kilometraje se
actualiza a 13,200.

### Registrar un mantenimiento

```http
POST /api/mantenimientos
Content-Type: application/json
```
```json
{
  "vehiculo": { "idVehiculo": 1 },
  "fechaMantenimiento": "2026-10-29",
  "descripcion": "Revision de frenos y rotacion de llantas",
  "costo": 2400.00,
  "kilometraje": 13200
}
```

### Ejemplos de error

**Matrícula duplicada — 409 Conflict**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un vehiculo con la matricula: VER-1001",
  "path": "/api/vehiculos"
}
```

**Sin credenciales — 401 Unauthorized**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/vehiculos"
}
```

**Fechas ocupadas — 409 Conflict**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El vehiculo ya tiene un alquiler que se traslapa con esas fechas",
  "path": "/api/alquileres"
}
```

---

## El frontend

La interfaz web se sirve desde `src/main/resources/static/` y consume la API con
`fetch()`. No comparte código con el backend ni conoce la base de datos: solo el
contrato HTTP.

Incluye:

- Pantalla de acceso que valida las credenciales contra la propia API.
- Panel con indicadores de flota, alquileres activos y devoluciones vencidas.
- Secciones de Vehículos, Clientes, Alquileres, Mantenimientos y Oficinas, cada
  una con alta y listado.
- Cálculo del precio estimado en vivo al armar un alquiler, con el desglose de
  los descuentos y recargos que aplica cada tipo de vehículo.

---

## Autor

Desarrollado como parte de la asignatura de Arquitectura Orientada a Servicios.
Universidad Cristóbal Colón — Ingeniería en Sistemas Computacionales.
