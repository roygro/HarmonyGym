package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Pago;
import com.example.harmonyGymBack.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Crear un nuevo pago (SIN DTO)
    public Pago crearPago(Pago pago) {
        // Asegurar valores por defecto
        if (pago.getCantidad() == null) {
            pago.setCantidad(1);
        }
        if (pago.getFechaVenta() == null) {
            pago.setFechaVenta(LocalDateTime.now());
        }
        return pagoRepository.save(pago);
    }

    // Obtener todos los pagos
    public List<Pago> obtenerTodosLosPagos() {
        return pagoRepository.findAllByOrderByFechaVentaDesc();
    }

    // Obtener pagos por cliente
    public List<Pago> obtenerPagosPorCliente(String folioCliente) {
        return pagoRepository.findByFolioCliente(folioCliente);
    }

    // Obtener pagos por recepcionista
    public List<Pago> obtenerPagosPorRecepcionista(String idRecepcionista) {
        return pagoRepository.findByIdRecepcionista(idRecepcionista);
    }

    // Obtener pago por ID
    public Pago obtenerPagoPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    // Obtener estadísticas del día
    public EstadisticasDiaDTO obtenerEstadisticasDelDia() {
        Double totalVentas = pagoRepository.findTotalVentasHoy();
        Long cantidadVentas = pagoRepository.countVentasHoy();

        totalVentas = totalVentas != null ? totalVentas : 0.0;
        cantidadVentas = cantidadVentas != null ? cantidadVentas : 0L;

        return new EstadisticasDiaDTO(totalVentas, cantidadVentas);
    }

    // Obtener pagos por rango de fechas
    public List<Pago> obtenerPagosPorRangoFechas(String fechaInicio, String fechaFin) {
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
        LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

        return pagoRepository.findByFechaVentaBetween(inicio, fin);
    }

    // DTO interno para estadísticas
    public static class EstadisticasDiaDTO {
        private Double totalVentas;
        private Long cantidadVentas;

        public EstadisticasDiaDTO() {}

        public EstadisticasDiaDTO(Double totalVentas, Long cantidadVentas) {
            this.totalVentas = totalVentas;
            this.cantidadVentas = cantidadVentas;
        }

        // Getters y Setters
        public Double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(Double totalVentas) { this.totalVentas = totalVentas; }

        public Long getCantidadVentas() { return cantidadVentas; }
        public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }
    }
}