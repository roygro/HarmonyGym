# 🔌 Integraciones de Terceros — HarmonyGym

## PayPal REST API

### ¿Qué API se utiliza?

**Proveedor:** PayPal  
**Producto:** PayPal REST API v2  
**Documentación oficial:** https://developer.paypal.com/docs/api/overview/

---

### ¿Para qué se utiliza?

La API de PayPal se integra en HarmonyGym para gestionar el **procesamiento de pagos de membresías**. Permite a los usuarios del gimnasio adquirir o renovar sus planes de suscripción (mensual, trimestral, anual) de forma segura, sin que el sistema tenga que manejar directamente datos sensibles de tarjetas de crédito.

**Funcionalidad que resuelve:**
- Cobro de membresías y planes de entrenamiento.
- Generación de órdenes de pago con monto y descripción del plan.
- Confirmación del pago y activación automática de la membresía en el sistema.

---

### ¿Cómo se utiliza?

#### Tipo de autenticación

Se utiliza **OAuth 2.0 con Client Credentials**. El backend solicita un `access_token` temporal usando las credenciales de la aplicación (`CLIENT_ID` y `CLIENT_SECRET`) almacenadas como variables de entorno. Este token se incluye en el encabezado `Authorization: Bearer <token>` de cada petición subsecuente.

```
POST https://api-m.sandbox.paypal.com/v1/oauth2/token
Authorization: Basic Base64(CLIENT_ID:CLIENT_SECRET)
Body: grant_type=client_credentials
```

#### Endpoints principales consumidos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/v2/checkout/orders` | Crea una orden de pago con el monto del plan seleccionado |
| `POST` | `/v2/checkout/orders/{id}/capture` | Captura y confirma el pago una vez aprobado por el usuario |

#### Flujo de la información

```
[Usuario elige plan]
       ↓
[Frontend → Backend]  POST /api/pagos/crear-orden
       ↓
[Backend → PayPal]    POST /v2/checkout/orders
       ↓
[PayPal → Backend]    { id: "ORDER_ID", status: "CREATED", links: [...] }
       ↓
[Backend → Frontend]  Devuelve la URL de aprobación de PayPal
       ↓
[Usuario aprueba el pago en PayPal]
       ↓
[Frontend → Backend]  POST /api/pagos/capturar-orden/:orderId
       ↓
[Backend → PayPal]    POST /v2/checkout/orders/{id}/capture
       ↓
[PayPal → Backend]    { status: "COMPLETED", ... }
       ↓
[Backend activa membresía en BD y notifica al frontend]
```

#### Variables de entorno requeridas (nunca exponer en el código)

```env
PAYPAL_CLIENT_ID=xxxxxxxxxxxxxxx
PAYPAL_CLIENT_SECRET=xxxxxxxxxxxxxxx
PAYPAL_MODE=sandbox   # cambiar a "live" en producción
```

---

### Entorno

- **Sandbox (desarrollo):** `https://api-m.sandbox.paypal.com`
- **Producción:** `https://api-m.paypal.com`

Actualmente el proyecto opera en **modo sandbox** para pruebas.

---

### Repositorios

- **Backend:** https://github.com/roygro/HarmonyGym
- **Frontend:** https://github.com/roygro/HarmonyGymFront
