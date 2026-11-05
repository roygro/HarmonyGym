# Registro de Cambios y Arquitectura — HarmonyGym Backend

Este documento detalla todas las modificaciones, correcciones arquitectónicas, nuevas capacidades de negocio, endpoints y decisiones tomadas en el backend de **HarmonyGym** (`com.example.harmonyGymBack`).

---

## 1. Resumen por Fases

### Fase 1 — Corrupción de datos y consistencia de catálogos
- **Generación de Folios Únicos (`MEMxxx`):** En `MembresiaService`, se asegura que siempre se genere un nuevo folio con formato `MEM001`, `MEM002`... cuando el `idMembresia` sea nulo, vacío o espacios, ignorando folios provistos por clientes para evitar sobreescritura de catálogos.
- **Enums Tipados y Converters:** Se unificaron los estatus dispersos en cadenas hacia enums fuertemente tipados con `@Converter(autoApply = true)` en `model/enums/`:
  - `EstatusPersona` (`Activo`, `Inactivo`)
  - `EstatusUsuario` (`Activo`, `Inactivo`, `Bloqueado`)
  - `EstatusCatalogo` (`Activa`, `Inactiva`)
  - `EstatusMembresiaCliente` (`Activa`, `Inactiva`, `Expirada`, `Cancelada`)
  - `EstatusInscripcion` (`Inscrito`, `Cancelado`, `Completado`)
  - `EstatusRutina` (`ACTIVO`, `INACTIVO`)
  - Lectura case-insensitive para compatibilidad con datos existentes y escritura normalizada.
- **Entidad de Asignación de Rutinas:** Se creó la entidad JPA `AsignacionRutina` (tabla `asigna`) con restricción `@UniqueConstraint(columnNames = {"folio_rutina", "cliente_asignado"})` y su respectivo `AsignacionRutinaRepository`, eliminando consultas SQL nativas no gestionadas.

### Fase 2 — Flujo Transaccional de Membresía y Pago
- **Enriquecimiento del Modelo `Pago` (`vende`):**
  - Campos agregados: `metodoPago` (`MetodoPago`: `EFECTIVO`, `TARJETA`, `TRANSFERENCIA`, `PAYPAL`), `estatusPago` (`EstatusPago`: `PENDIENTE`, `PAGADO`, `CANCELADO`), `montoRecibido` (BigDecimal), `cambio` (BigDecimal), `referenciaExterna` (String) y relación `@ManyToOne` hacia `MembresiaCliente`.
  - Relaciones JPA reales `@ManyToOne` hacia `Cliente`, `Recepcionista` y `Producto`.
- **Cálculo de Precios en el Servidor (`CalculadoraPrecioService`):**
  - Fórmula oficial: `meses = ceil(plan.duracionDias / membresia.duracion)`, mínimo 1 mes.
  - `total = precioBase * meses * factorDescuento`, redondeado con `BigDecimal` y `RoundingMode.HALF_UP`.
  - `fechaFin = fechaInicio + duracionDias - 1 día`.
  - Endpoint `GET /api/membresias-clientes/cotizar`.
- **Venta Atómica de Membresías (`VentaService`):**
  - Operación `POST /api/ventas/membresia`: crea en una sola transacción el registro de pago y la membresía del cliente activa. En efectivo valida monto suficiente y calcula cambio exacto.
  - Integración real con PayPal: crea orden vía API Sandbox, registra pago en `PENDIENTE` y membresía `Inactiva`, y al capturar activa la membresía.
  - Renovación (`POST /api/ventas/membresia/renovar`), cambio con prorrateo (`POST /api/ventas/membresia/cambiar`) y cancelación (`POST /api/ventas/membresia/cancelar`).
  - Venta de productos con bloqueo pesimista `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `ProductoRepository` para evitar stock negativo.

### Fase 3 — Reglas de Negocio del Gimnasio Real
- **Vencimiento Automático de Membresías:** Habilitado `@EnableScheduling` y creado `VencimientoMembresiasTask` (cron `0 5 0 * * *` en zona horaria `America/Mexico_City` y ejecución al arranque vía `ApplicationRunner`) que marca como `Expirada` las membresías con `fechaFin < hoy`. Corregido `isVigente()` en `MembresiaCliente`.
- **Inscripciones y Concurrencia de Cupo:**
  - Bloqueo pesimista en `ActividadRepository.findByIdWithLock` para asegurar que el cupo nunca sobrepase `cupoMaximo`.
  - Validaciones estrictas: cliente activo, membresía vigente, actividad futura activa, prevención de traslapes de horario del cliente, prevención de doble inscripción.
  - Endpoint `PUT /api/actividades/{id}/asistencia` para que instructores registren asistencia (`asistio`, estatus `Completado`).
  - Registro de calificaciones (1 a 5 estrellas) restringido a clientes con asistencia confirmada.
  - Desactivación de actividades cancela inscripciones en cascada notificando motivo.
- **Desactivación en Cascada:**
  - Desactivar o eliminar instructores/recepcionistas inhabilita su usuario (`EstatusUsuario.Inactivo`).
  - Protección del último Administrador activo y prevención de auto-eliminación o auto-desactivación.
- **Inicializador de Datos (`DataInitializer`):** Se cargan roles `ROL001-ROL004`, los 4 planes de pago estándar (`PlanMensual`, `PlanTrimestral`, `PlanSemestral`, `PlanAnual`) y usuarios de prueba.

### Fase 4 — Arquitectura, Excepciones y DTOs
- **Validaciones:** Agregado `spring-boot-starter-validation`.
- **DTOs de Entrada y Salida:** Creados en `dto/request/` y `dto/response/` (`VentaMembresiaRequest`, `VentaProductoRequest`, `CrearActividadRequest`, `ApiResponse`).
- **Manejador Global de Excepciones:** `@RestControllerAdvice` (`GlobalExceptionHandler`) con respuestas homogéneas `{ success, mensaje, errores }` para `ReglaNegocioException`, `RecursoNoEncontradoException`, `DatosInvalidosException`, etc.
- **Logging SLF4J:** Sustituidos todos los `System.out` y `System.err` por logger SLF4J en controladores y servicios.
- **Perfiles Spring:** `application-dev.properties` (logging DEBUG, show-sql activado) y `application-prod.properties` (logging INFO, show-sql desactivado).
- **Inyección por Constructor:** Convertidas todas las clases de controlador y servicio a inyección por constructor con campos `final`.
- **Pruebas Unitarias Automatizadas:**
  - `CalculadoraPrecioServiceTest`: Pruebas de planes mensual, trimestral, semestral, anual y redondeo HALF_UP.
  - `VentaServiceUnitTest`: Cobro en efectivo con cambio exacto, cobro insuficiente rechazado y flujo PayPal.
  - `ClienteActividadServiceTest`: Pruebas de cupo disponible, sobrecupo rechazado y validación de membresía vencida.

### Fase 5 — Seguridad Integral
- **BCrypt:** Contraseñas hasheadas con `BCryptPasswordEncoder(10)`. Migración automática en el primer login de contraseñas legacy en texto plano.
- **Contraseñas Temporales Seguras:** `PasswordHelper` usando `SecureRandom`, longitud 12 con mayúsculas, minúsculas, dígitos y símbolos.
- **Flag `debeCambiarPassword`:** Los usuarios creados por el sistema deben actualizar su contraseña en su primer acceso.
- **JWT (HMAC-SHA256):** `JwtTokenProvider` emite tokens con duración de 8 horas conteniendo `idUsuario`, `tipoUsuario`, `rol` y `idPersona`.
- **Filtro de Seguridad:** `JwtAuthenticationFilter` y `SecurityConfig` basados en `SecurityFilterChain` de Spring Security 6.
- **CORS Configurado:** Permite orígenes definidos en variable de entorno `FRONT_URL` (por defecto `http://localhost:4200`).
- **Autorización por Roles (`@PreAuthorize`):**
  - Solo Administrador: administración de instructores, recepcionistas, administradores y reportes.
  - Administrador y Recepcionista: ventas, cobros, catálogo de clientes y pagos.
  - Instructor: asistencia y consulta de rutinas.
  - Cliente: restricción por token en `/api/clientes/{folioCliente}` asegurando que solo pueda ver sus propios datos.

---

## 2. Catálogo de Endpoints

### Endpoints Nuevos
- `GET /api/membresias-clientes/cotizar`: Cotiza total, desglose de meses, factor y fechas sin persistir.
- `POST /api/ventas/membresia`: Cobro y asignación atómica de membresía.
- `POST /api/ventas/producto`: Venta de productos con control pesimista de stock.
- `POST /api/ventas/{idPago}/paypal/capturar`: Captura orden PayPal y activa membresía.
- `POST /api/ventas/membresia/renovar`: Renovación de membresía con extensión de fechas.
- `POST /api/ventas/membresia/cambiar`: Cambio de plan o membresía con cálculo de prorrateo.
- `POST /api/ventas/membresia/cancelar`: Cancelación con cálculo de reembolso proporcional.
- `PUT /api/actividades/{id}/asistencia`: Registro de asistencia por el instructor.

### Endpoints Modificados
- `POST /api/auth/login`: Retorna token JWT, `debeCambiarPassword` y migra contraseñas legacy a BCrypt.
- `GET /api/clientes/{folioCliente}`: Protegido con `@PreAuthorize("@seguridadAcceso.puedeAccederCliente(#folioCliente, authentication)")`.
- `POST /api/clientes`: Genera contraseña temporal de 12 caracteres aleatoria con `SecureRandom`.
- `POST /api/membresias`: Genera folio `MEMxxx` en servidor e ignora idCliente entrante.
- `/api/administradores/**`, `/api/recepcionistas/**`, `/api/ventas/**`, `/api/pagos/**`: Protegidos por roles (`ROLE_ADMINISTRADOR`, `ROLE_RECEPCIONISTA`).

### Endpoints Obsoletos / Eliminados
- Métodos duplicados sin transacción en asignación directa de membresías sin cobro previo.

---

## 3. Decisiones Tomadas

| Decisión | Elección Realizada | Justificación |
|---|---|---|
| **Estatus como Enum** | Enums con `@Converter(autoApply = true)` | Garantiza integridad referencial y tipado fuerte, aceptando datos existentes mayúsculas/minúsculas sin romper la BD ni el contrato JSON. |
| **Migración de Contraseñas** | En login si no inicia con `$2a$`, migra a BCrypt | No requiere downtime ni reseteo forzado de credenciales existentes; actualiza las contraseñas al primer acceso. |
| **Control de Concurrencia** | `PESSIMISTIC_WRITE` en Actividades y Productos | Previene sobrecupos y stock negativo cuando múltiples recepcionistas o clientes compran o se inscriben simultáneamente. |
| **Planes de Pago** | Herencia de `PlanPago` con Discriminator | Permite polimorfismo para cálculo de fechas y duraciones sin duplicidad de tablas. |
| **Generador de Credenciales de Prueba** | Inicialización automática en `DataInitializer` | Asegura que las credenciales oficiales de evaluación (`admin/Admin123`, `recepcion/Recep123`, `instructor/Instr123`, `cliente/Cliente123`) funcionen en cualquier base de datos nueva o existente. |

---

## 4. Guía de Prueba y Verificación

### Credenciales de Acceso
| Rol | Usuario | Contraseña | Capacidades Principales |
|---|---|---|---|
| **Administrador** | `admin` | `Admin123` | Gestión total, personal, catálogo de membresías y reportes |
| **Recepcionista** | `recepcion` | `Recep123` | Registro de clientes, ventas de membresía y cobro de productos |
| **Instructor** | `instructor` | `Instr123` | Control de asistencia a clases y asignación de rutinas |
| **Cliente** | `cliente` | `Cliente123` | Consulta de membresía propia, inscripción a actividades |

### Pasos de Verificación Recomendados
1. **Compilación y Pruebas Backend:**
   ```powershell
   cd C:\Harmony\HarmonyGym
   .\mvnw.cmd test
   .\mvnw.cmd -q -DskipTests package
   ```
2. **Arranque del Backend:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. **Flujo de Venta y Pago:**
   - Iniciar sesión como `recepcion`.
   - Ir a la pantalla de Cobro / Venta.
   - Seleccionar un cliente, membresía Básica y Plan Mensual.
   - Comprobar que el valor total lo cotiza el backend (`/cotizar`).
   - Pagar en efectivo con un monto mayor y comprobar el cambio devuelto.
4. **Flujo de Seguridad:**
   - Intentar acceder con token de `cliente` a los datos de otro cliente en `/api/clientes/CLI999` y verificar respuesta `403 Forbidden`.
