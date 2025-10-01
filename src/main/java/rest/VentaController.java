package rest;

import model.DetalleVenta;
import model.Venta;
import service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // ✅ AGREGAR ESTE IMPORT

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // Registrar nueva venta
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequest ventaRequest) {
        try {
            Venta venta = ventaService.procesarVenta(
                    ventaRequest.getDetallesVenta(),
                    ventaRequest.getVendedor(),
                    ventaRequest.getMetodoPago()
            );
            return ResponseEntity.ok(venta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Obtener todas las ventas
    @GetMapping
    public List<Venta> getAllVentas() {
        return ventaService.getAllVentas();
    }

    // Obtener venta por ID - ✅ CORREGIDO
    @GetMapping("/{id}")
    public ResponseEntity<Venta> getVentaById(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.getVentaById(id); // ✅ Usar Optional explícitamente
        if (venta.isPresent()) {
            return ResponseEntity.ok(venta.get()); // ✅ .get() para obtener el objeto
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener ventas por vendedor
    @GetMapping("/vendedor/{vendedor}")
    public List<Venta> getVentasByVendedor(@PathVariable String vendedor) {
        return ventaService.getVentasByVendedor(vendedor);
    }

    // Clase interna para recibir los datos de la venta
    public static class VentaRequest {
        private List<DetalleVenta> detallesVenta;
        private String vendedor;
        private String metodoPago;

        // Getters y Setters
        public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
        public void setDetallesVenta(List<DetalleVenta> detallesVenta) { this.detallesVenta = detallesVenta; }

        public String getVendedor() { return vendedor; }
        public void setVendedor(String vendedor) { this.vendedor = vendedor; }

        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    }
}