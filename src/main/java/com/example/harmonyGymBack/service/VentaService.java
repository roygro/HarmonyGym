package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.DetalleVenta;
import com.example.harmonyGymBack.model.Venta;
import com.example.harmonyGymBack.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // ✅ AGREGAR ESTE IMPORT

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    // Procesar una venta completa
    @Transactional
    public Venta procesarVenta(List<DetalleVenta> detallesVenta, String vendedor, String metodoPago) {

        // Validar stock antes de procesar
        if (!validarStockDisponible(detallesVenta)) {
            throw new RuntimeException("Stock insuficiente para uno o más productos");
        }

        // Calcular total
        Double total = calcularTotalVenta(detallesVenta);

        // Crear venta
        Venta venta = new Venta(total, vendedor, metodoPago);

        // Asignar detalles a la venta
        for (DetalleVenta detalle : detallesVenta) {
            venta.getProductos().add(detalle);

            // Actualizar stock
            productoService.actualizarStock(
                    detalle.getProducto().getId(),
                    detalle.getCantidad()
            );
        }

        // Guardar venta
        return ventaRepository.save(venta);
    }

    // Calcular total de la venta
    private Double calcularTotalVenta(List<DetalleVenta> detallesVenta) {
        return detallesVenta.stream()
                .mapToDouble(detalle -> detalle.getPrecioUnitario() * detalle.getCantidad())
                .sum();
    }

    // Validar que haya stock para todos los productos
    private boolean validarStockDisponible(List<DetalleVenta> detallesVenta) {
        for (DetalleVenta detalle : detallesVenta) {
            boolean stockSuficiente = productoService.validarStockSuficiente(
                    detalle.getProducto().getId(),
                    detalle.getCantidad()
            );
            if (!stockSuficiente) {
                return false;
            }
        }
        return true;
    }

    // Obtener todas las ventas
    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    // Obtener venta por ID
    public Optional<Venta> getVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    // Obtener ventas por vendedor
    public List<Venta> getVentasByVendedor(String vendedor) {
        return ventaRepository.findByVendedor(vendedor);
    }
}