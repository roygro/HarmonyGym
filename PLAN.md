# Plan de Refactorización y Mejoras de Arquitectura - HarmonyGym

## Resumen General
Este plan define las acciones por fase para modernizar y estabilizar el sistema HarmonyGym (Backend Spring Boot 3.5.6 + Java 17 y Frontend Angular 20).
El trabajo se ejecuta de forma secuencial y autónoma en la rama `mejoras-antigravity` de cada repositorio.

---

## FASE 1 — Corrupción de datos (crítico)

### Back (`C:\Harmony\HarmonyGym`)
1. **1.1 Folio de membresías:**
   - Modificar `MembresiaService.java`:
     - Generar automáticamente el folio `MEMxxx` cuando `idMembresia` sea `null`, vacío `""` o contenga solo espacios en blanco.
     - Ignorar siempre cualquier id enviado por el cliente al momento de crear.
   - Migración idempotente en `DataInitializer.java` o runner:
     - Detectar filas en `membresia` con `id_membresia = ''` (o en blanco), asignar el siguiente folio correlativo y actualizar llaves foráneas/referencias en `cliente_membresia.id_membresia` y `vende.id_membresia`.
2. **1.2 Estatus unificados:**
   - Crear paquete `com.example.harmonyGymBack.model.enums` con:
     - `EstatusPersona.java` (Activo, Inactivo)
     - `EstatusUsuario.java` (Activo, Inactivo, Bloqueado)
     - `EstatusCatalogo.java` (Activa, Inactiva)
     - `EstatusMembresiaCliente.java` (Activa, Inactiva, Expirada, Cancelada)
     - `EstatusInscripcion.java` (Inscrito, Cancelado, Completado)
     - `EstatusRutina.java` (Activa, Inactiva)
   - Crear `AttributeConverter` con `@Converter(autoApply = true)` para cada enum: lectura case-insensitive y persistencia en formato capitalizado.
   - Actualizar entidades (`Cliente`, `Instructor`, `Recepcionista`, `Administrador`, `Usuario`, `Membresia`, `Actividad`, `MembresiaCliente`, `RealizaActividad`, `RutinaEntity`).
   - Actualizar repositorios y servicios reemplazando cadenas mágicas por constantes de los enums.
3. **1.3 Tabla de asignación de rutinas (`asigna`):**
   - Crear entidad `AsignacionRutina.java` mapeada a la tabla `asigna` con `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`, `@ManyToOne` a `RutinaEntity` (`folio_rutina`), `@ManyToOne` a `Cliente` (`cliente_asignado`), `@ManyToOne` a `Instructor` (`folio_instructor`), campo `fechaAsignacion` y restricción `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"folio_rutina", "cliente_asignado"}))`.
   - Crear `AsignacionRutinaRepository.java`.
   - Reemplazar consultas nativas en `RutinaRepository.java` y `RutinaServiceImpl.java` por consultas JPA / repositorio. Validar duplicados devolviendo error de negocio claro.

### Front (`C:\Harmony\HarmonyGymFront`)
1. **1.1 Formulario de membresía:**
   - Modificar `membresia-form.ts`: no enviar `idMembresia` en la carga útil al crear una nueva membresía.

---

## FASE 2 — Flujo de membresía y pago (crítico)

### Back (`C:\Harmony\HarmonyGym`)
1. **2.1 Modelo de Pago (`vende`):**
   - Agregar campos a `Pago.java`: `metodoPago` (`MetodoPago`), `estatusPago` (`EstatusPago`), `montoRecibido` (`BigDecimal`), `cambio` (`BigDecimal`), `referenciaExterna` (`String`), `@ManyToOne` opcional a `MembresiaCliente`.
   - Convertir relaciones `folioCliente`, `idRecepcionista`, `codigoProducto` a `@ManyToOne` reales sobre `Cliente`, `Recepcionista`, `Producto`.
   - Enums: `MetodoPago { EFECTIVO, TARJETA, TRANSFERENCIA, PAYPAL }` y `EstatusPago { PENDIENTE, PAGADO, CANCELADO }`.
2. **2.2 Regla de cálculo de precios:**
   - Crear `CalculadoraPrecioService.java`:
     - `meses = Math.max(1, (int) Math.ceil((double) plan.duracionDias / membresia.duracion))`.
     - `total = membresia.precio * meses * factorDescuento` con `BigDecimal` y `RoundingMode.HALF_UP`.
     - `fechaFin = fechaInicio + plan.duracionDias - 1 dia`.
   - Endpoint `GET /api/membresias-clientes/cotizar?idMembresia=&idPlanPago=`.
3. **2.3 Venta de membresía:**
   - Endpoint transaccional `POST /api/ventas/membresia`.
   - Validar cliente activo, plan activo, membresía activa y ausencia de membresía activa vigente.
   - Cálculo en el servidor (ignorar total del cliente).
   - Manejo según método de pago (EFECTIVO valida montoRecibido >= total y cambio; TARJETA/TRANSFERENCIA; PAYPAL crea PENDIENTE / Inactiva y orden en PayPal).
   - Recepcionista obtenido del contexto de seguridad.
4. **2.4 PayPal Real:**
   - `PaypalService.java`: cliente con timeout en `RestTemplate`, crear orden y capturar orden validando status `COMPLETED` y coincidencia de importe.
   - `POST /api/ventas/{idPago}/paypal/capturar`.
5. **2.5 Renovación, cambio y cancelación:**
   - `POST /api/ventas/membresia/renovar`.
   - `POST /api/ventas/membresia/cambiar` (prorrateo por días restantes).
   - Cancelación sin reembolso (cambio de estatus, registro de usuario y fecha).
6. **2.6 Venta de productos:**
   - `POST /api/ventas/producto` con bloqueo pesimista `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `Producto`.
7. **2.7 Limpieza:**
   - Eliminar `GET /api/pagos/tipos-membresia`.

### Front (`C:\Harmony\HarmonyGymFront`)
1. Actualizar `pago-create.ts` y HTML: usar cotización del back, eliminar cálculo local y llamada al nuevo endpoint de venta. Integración de flujo PayPal con captura y NotificacionService.
2. Actualizar `cliente-membresia.ts`: enlazar botones de renovación y cambio a los nuevos endpoints de venta.
3. Actualizar servicios: `pago.service.ts`, `membresia-cliente.service.ts`, `venta.service.ts`.

---

## FASE 3 — Reglas faltantes

### Back (`C:\Harmony\HarmonyGym`)
1. **3.1 Vencimiento automático:**
   - Habilitar `@EnableScheduling`. Tarea `@Scheduled(cron = "0 5 0 * * *", zone = "America/Mexico_City")` que actualice membresías vencidas a `Expirada`.
   - Ejecución automática también al iniciar (`@PostConstruct` o `CommandLineRunner`).
   - Corregir cálculo de `isVigente()` (incluir último día).
2. **3.2 Clases e inscripciones:**
   - Actividad: `cupoMaximo` configurable; cupo disponible = `cupoMaximo - inscripcionesActivas`.
   - Bloqueo pesimista `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `Actividad` al inscribir.
   - Validaciones de cliente activo, membresía vigente, horario futuro y evitar traslapes de horario del cliente o choques de horario de instructor/sala.
   - Nuevo endpoint `PUT /api/actividades/{id}/asistencia` para el instructor.
   - Persistencia de calificación en `PUT /api/cliente-actividades/calificar` (1 a 5 estrellas, solo si asistió).
   - Desactivar clase cancela inscripciones con motivo "Clase cancelada".
3. **3.3 Personal y usuarios:**
   - Desactivación lógica de personas propaga estatus Inactivo a su `Usuario`.
   - Protección: no eliminar ni desactivar al último administrador activo, ni permitir que un administrador se auto-elimine.
4. **3.4 Datos iniciales idempotentes:**
   - `DataInitializer.java`: sembrado de roles `ROL001-ROL004` y planes (Mensual, Trimestral, Semestral, Anual).
   - Ajustar `asignarRolPorDefecto` para buscar por `nombreRol = tipoUsuario`.

### Front (`C:\Harmony\HarmonyGymFront`)
1. Incorporar interfaz de asistencia en el módulo de actividades del instructor.
2. Manejo de mensajes de error de reglas de negocio vía `NotificacionService`.

---

## FASE 4 — Arquitectura del back

### Back (`C:\Harmony\HarmonyGym`)
1. DTOs de petición y respuesta en `dto/request/` y `dto/response/`.
2. Validaciones con `spring-boot-starter-validation` (`@Valid`, `@NotBlank`, etc.).
3. Jerarquía de excepciones de negocio (`RecursoNoEncontradoException`, `ReglaNegocioException`, `DatosInvalidosException`) y `@RestControllerAdvice` uniforme: `{ "success": false, "mensaje": "...", "errores": { ... } }`.
4. Inyección por constructor con campos `final`.
5. Reemplazar `System.out`/`System.err` por logging SLF4J.
6. Perfiles `application-dev.properties` y `application-prod.properties`.
7. Pruebas unitarias e integración JUnit 5 con H2 en modo PostgreSQL.

### Front (`C:\Harmony\HarmonyGymFront`)
1. Adaptar servicios para procesar el formato uniforme de respuesta y errores del back.

---

## FASE 5 — Seguridad

### Back (`C:\Harmony\HarmonyGym`)
1. Integración de Spring Security y JWT (`spring-boot-starter-security`, `jjwt`).
2. Encriptación BCrypt y migración automática al inicio para contraseñas en texto plano existentes.
3. Generación de contraseñas seguras con `SecureRandom` y bandera `debeCambiarPassword`.
4. Filtro de autenticación JWT y endpoint `POST /api/auth/login`.
5. Control de autorización con `@PreAuthorize` según roles y verificación de acceso a folios propios para el rol Cliente.
6. Configuración CORS global parametrizada por `FRONT_URL`.

### Front (`C:\Harmony\HarmonyGymFront`)
1. Interceptor HTTP para inyectar token `Authorization: Bearer <token>` y redirigir al login ante respuestas 401.
2. Actualización de `roleGuard` para verificar presencia y validez del token.
3. Flujo obligatorio de cambio de contraseña para usuarios con `debeCambiarPassword = true`.

---

## FASE 6 — Arquitectura del front

### Front (`C:\Harmony\HarmonyGymFront`)
1. Configuración de `src/environments/environment.ts` y `environment.development.ts` con `apiUrl`. Reemplazo de URLs hardcodeadas.
2. Centralización de modelos en `src/app/models/`.
3. Encapsulamiento de llamadas HTTP exclusivamente en servicios.
4. Refactorización de componentes extensos (> 1,100 líneas) en subcomponentes modulares conservando estética negro y rojo.
5. Limpieza de código muerto, diálogos legacy y console logs en producción.
6. Completar navegación y menú por rol.
7. Conectar `/administradores/dashboard` con endpoints de métricas reales.
