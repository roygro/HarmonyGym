# HarmonyGym — Backend (Spring Boot)

Sistema web para la administración de gimnasio HarmonyGym (Desarrollo Web Integral, UTNG).

---

## Cómo iniciar

### Requisitos previos
- **Java 17 o superior** (OpenJDK / Oracle JDK 17 o 21).
- Conexión a Internet para la base de datos PostgreSQL en Neon y dependencias de Maven.

### Pasos de arranque
1. **Configurar variables de entorno**:
   Copia la plantilla `.env.example` como `.env` en la raíz de esta carpeta (`HarmonyGym`):
   ```powershell
   Copy-Item .env.example .env
   ```
   Asegúrate de que las credenciales de base de datos (PostgreSQL/Neon), correo Gmail y PayPal Sandbox estén configuradas en tu `.env`. Spring Boot carga automáticamente este archivo mediante `spring.config.import=optional:file:.env[.properties]`.

2. **Arrancar el servidor**:
   Ejecuta el siguiente comando desde la raíz de `HarmonyGym`:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   El backend arrancará en el puerto **8081** (`http://localhost:8081`).

3. **Verificar estado de la API**:
   - Healthcheck: `GET http://localhost:8081/api/auth/status` (responde HTTP 200).

---

## Usuarios de prueba

El sistema cuenta con 4 usuarios preconfigurados en la base de datos con las contraseñas cifradas en BCrypt:

| Usuario | Contraseña | Rol / Tipo | Acceso principal |
| :--- | :--- | :--- | :--- |
| `admin` | `Admin123` | **Administrador** | Gestión de catálogos, reportes, usuarios y finanzas |
| `recepcion` | `Recep123` | **Recepcionista** | Registro de clientes, cobro de membresías y ventas |
| `instructor` | `Instr123` | **Instructor** | Asignación de rutinas y seguimiento de actividades |
| `cliente` | `Cliente123` | **Cliente** | Visualización de membresías, rutinas y pagos |

---

## Pruebas automatizadas

Para compilar y ejecutar las pruebas unitarias y de integración:
```powershell
.\mvnw.cmd clean package
```
O solo pruebas:
```powershell
.\mvnw.cmd test
```

---

## Notas de la integración

1. **Carga transparente de configuración `.env`**:
   - Se configuró `spring.config.import=optional:file:.env[.properties]` en `src/main/resources/application.properties` como primera línea.
   - Las variables de entorno de Windows siguen teniendo prioridad estándar si se definen explícitamente.
   - Se aseguró que `.env`, `target/` y `.idea/workspace.xml` queden estrictamente excluidos en `.gitignore`.
2. **Corrección de colisión de variables de entorno de sistema**:
   - Se detectó que si el entorno de Windows tenía residualmente `DB_PASSWORD=root` definido a nivel de usuario, sobrescribía la contraseña de Neon. Se documentó y corrigió para garantizar que la variable de `.env` se aplique limpiamente al backend.
3. **Exposición pública del health check**:
   - Se habilitó `.requestMatchers(HttpMethod.GET, "/api/auth/status").permitAll()` en `SecurityConfig` para permitir verificar el estado del backend sin requerir token JWT previo.
4. **Integración limpia en `main`**:
   - La rama `mejoras-antigravity` (Fases 1 a 6) fue integrada a `main` con `--no-ff` conservando el historial y validando 17 pruebas unitarias exitosas.
