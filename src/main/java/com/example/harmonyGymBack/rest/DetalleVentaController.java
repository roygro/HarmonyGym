package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.DetalleVenta;
import com.example.harmonyGymBack.service.DetalleVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // ✅ AGREGAR ESTE IMPORT

@RestController
@RequestMapping("/api/detalle-venta")
@CrossOrigin(origins = "*")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    // Obtener todos los detalles de venta
    @GetMapping
    public List<DetalleVenta> getAllDetallesVenta() {
        return detalleVentaService.getAllDetallesVenta(); // ✅ CORREGIDO
    }

    // Obtener detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetalleVenta> getDetalleVentaById(@PathVariable Long id) {
        Optional<DetalleVenta> detalle = detalleVentaService.getDetalleVentaById(id); // ✅ CORREGIDO
        return detalle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear detalle de venta individual
    @PostMapping
    public DetalleVenta createDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        return detalleVentaService.saveDetalleVenta(detalleVenta);
    }
}