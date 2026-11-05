package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.service.PaypalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = "*")
public class PaypalController {

    @Autowired
    private PaypalService paypalService;

    /**
     * @deprecated Usar POST /api/ventas/membresia con metodoPago=PAYPAL donde el total es calculado por el servidor.
     */
    @Deprecated
    @PostMapping("/crear-orden")
    public ResponseEntity<Map<String, Object>> crearOrden(@RequestBody Map<String, String> body) {
        String monto = body.get("monto");
        String descripcion = body.get("descripcion");
        Map<String, Object> orden = paypalService.crearOrden(monto, descripcion);
        return ResponseEntity.ok(orden);
    }

    /**
     * @deprecated Usar POST /api/ventas/{idPago}/paypal/capturar que actualiza el estado de la venta y membresía atómicamente.
     */
    @Deprecated
    @PostMapping("/capturar-orden/{orderId}")
    public ResponseEntity<Map<String, Object>> capturarOrden(@PathVariable String orderId) {
        Map<String, Object> resultado = paypalService.capturarOrden(orderId);
        return ResponseEntity.ok(resultado);
    }
}
