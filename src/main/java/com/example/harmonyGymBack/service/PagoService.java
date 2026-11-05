package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.Pago;
import com.example.harmonyGymBack.model.Producto;
import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.model.enums.EstatusPago;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.model.enums.MetodoPago;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.PagoRepository;
import com.example.harmonyGymBack.repository.ProductoRepository;
import com.example.harmonyGymBack.repository.RecepcionistaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;

    private final ClienteRepository clienteRepository;

    private final RecepcionistaRepository recepcionistaRepository;

    private final ProductoRepository productoRepository;

    public PagoService(PagoRepository pagoRepository, ClienteRepository clienteRepository, RecepcionistaRepository recepcionistaRepository, ProductoRepository productoRepository) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
        this.recepcionistaRepository = recepcionistaRepository;
        this.productoRepository = productoRepository;
    }


    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Pago crearPago(Pago pago) {
        if (pago.getCantidad() == null) {
            pago.setCantidad(1);
        }
        if (pago.getFechaVenta() == null) {
            pago.setFechaVenta(LocalDateTime.now());
        }
        if (pago.getMetodoPago() == null) {
            pago.setMetodoPago(MetodoPago.EFECTIVO);
        }
        if (pago.getEstatusPago() == null) {
            pago.setEstatusPago(EstatusPago.PAGADO);
        }

        // Resolver relaciones si vinieron como Strings o nulas
        if (pago.getCliente() == null && pago.getFolioCliente() != null) {
            clienteRepository.findById(pago.getFolioCliente()).ifPresent(pago::setCliente);
        }
        if (pago.getRecepcionista() == null) {
            if (pago.getIdRecepcionista() != null) {
                recepcionistaRepository.findById(pago.getIdRecepcionista()).ifPresent(pago::setRecepcionista);
            }
            if (pago.getRecepcionista() == null) {
                recepcionistaRepository.findByEstatus(EstatusPersona.Activo).stream().findFirst()
                        .ifPresent(pago::setRecepcionista);
            }
        }
        if (pago.getProducto() == null && pago.getCodigoProducto() != null) {
            productoRepository.findById(pago.getCodigoProducto()).ifPresent(pago::setProducto);
        }

        return pagoRepository.save(pago);
    }

    public List<Pago> obtenerTodosLosPagos() {
        return pagoRepository.findAllByOrderByFechaVentaDesc();
    }

    public List<Pago> obtenerPagosPorCliente(String folioCliente) {
        return pagoRepository.findByFolioCliente(folioCliente);
    }

    public List<Pago> obtenerPagosPorRecepcionista(String idRecepcionista) {
        return pagoRepository.findByIdRecepcionista(idRecepcionista);
    }

    public Pago obtenerPagoPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    public EstadisticasDiaDTO obtenerEstadisticasDelDia() {
        Double totalVentas = pagoRepository.findTotalVentasHoy();
        Long cantidadVentas = pagoRepository.countVentasHoy();

        totalVentas = totalVentas != null ? totalVentas : 0.0;
        cantidadVentas = cantidadVentas != null ? cantidadVentas : 0L;

        return new EstadisticasDiaDTO(totalVentas, cantidadVentas);
    }

    public List<Pago> obtenerPagosPorRangoFechas(String fechaInicio, String fechaFin) {
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
        LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

        return pagoRepository.findByFechaVentaBetween(inicio, fin);
    }

    public static class EstadisticasDiaDTO {
        private Double totalVentas;
        private Long cantidadVentas;

        public EstadisticasDiaDTO() {}

        public EstadisticasDiaDTO(Double totalVentas, Long cantidadVentas) {
            this.totalVentas = totalVentas;
            this.cantidadVentas = cantidadVentas;
        }

        public Double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(Double totalVentas) { this.totalVentas = totalVentas; }

        public Long getCantidadVentas() { return cantidadVentas; }
        public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }
    }
}
