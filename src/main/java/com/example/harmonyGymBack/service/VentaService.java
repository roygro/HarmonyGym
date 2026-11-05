package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.*;
import com.example.harmonyGymBack.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final ClienteRepository clienteRepository;

    private final MembresiaRepository membresiaRepository;

    private final PlanPagoRepository planPagoRepository;

    private final MembresiaClienteRepository membresiaClienteRepository;

    private final PagoRepository pagoRepository;

    private final ProductoRepository productoRepository;

    private final RecepcionistaRepository recepcionistaRepository;

    private final CalculadoraPrecioService calculadoraPrecioService;

    private final PaypalService paypalService;

    public VentaService(ClienteRepository clienteRepository, MembresiaRepository membresiaRepository, PlanPagoRepository planPagoRepository, MembresiaClienteRepository membresiaClienteRepository, PagoRepository pagoRepository, ProductoRepository productoRepository, RecepcionistaRepository recepcionistaRepository, CalculadoraPrecioService calculadoraPrecioService, PaypalService paypalService) {
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.planPagoRepository = planPagoRepository;
        this.membresiaClienteRepository = membresiaClienteRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.recepcionistaRepository = recepcionistaRepository;
        this.calculadoraPrecioService = calculadoraPrecioService;
        this.paypalService = paypalService;
    }


    private Recepcionista obtenerRecepcionistaCobro() {
        List<Recepcionista> activos = recepcionistaRepository.findByEstatus(EstatusPersona.Activo);
        if (!activos.isEmpty()) {
            return activos.get(0);
        }
        return recepcionistaRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No hay recepcionistas configurados para registrar el cobro"));
    }

    // ==================== 2.3 VENTA DE MEMBRESÍA ====================

    @Transactional
    public Map<String, Object> venderMembresia(String folioCliente, String idMembresia, Long idPlanPago,
                                                LocalDate fechaInicio, MetodoPago metodoPago, BigDecimal montoRecibido) {
        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con folio: " + folioCliente));
        if (!EstatusPersona.Activo.equals(cliente.getEstatus())) {
            throw new RuntimeException("El cliente no está activo");
        }

        Membresia membresia = membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada con ID: " + idMembresia));
        if (!EstatusCatalogo.Activa.equals(membresia.getEstatus())) {
            throw new RuntimeException("La membresía no está activa");
        }

        PlanPago plan = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado con ID: " + idPlanPago));
        if (!EstatusPersona.Activo.equals(plan.getEstatus())) {
            throw new RuntimeException("El plan de pago no está activo");
        }

        // Validar que no tenga otra membresía activa vigente (fechaFin >= hoy)
        Optional<MembresiaCliente> activaOpt = membresiaClienteRepository
                .findByClienteFolioClienteAndEstatus(folioCliente, EstatusMembresiaCliente.Activa);
        if (activaOpt.isPresent()) {
            MembresiaCliente mc = activaOpt.get();
            if (mc.getFechaFin() != null && !mc.getFechaFin().isBefore(LocalDate.now())) {
                throw new RuntimeException("El cliente ya tiene una membresía activa vigente hasta " + mc.getFechaFin() + ". Use el flujo de renovación.");
            }
        }

        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }

        CalculadoraPrecioService.ResultadoCalculo cotizacion = calculadoraPrecioService.calcular(membresia, plan, fechaInicio);
        BigDecimal total = cotizacion.getTotal();

        Recepcionista recepcionista = obtenerRecepcionistaCobro();

        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setRecepcionista(recepcionista);
        pago.setFechaVenta(LocalDateTime.now());
        pago.setCantidad(1);
        pago.setPrecioUnitario(total.doubleValue());
        pago.setTotal(total.doubleValue());
        pago.setMetodoPago(metodoPago);
        pago.setIdMembresia(idMembresia);

        MembresiaCliente membresiaCliente = new MembresiaCliente();
        membresiaCliente.setCliente(cliente);
        membresiaCliente.setMembresia(membresia);
        membresiaCliente.setPlanPago(plan);
        membresiaCliente.setFechaInicio(cotizacion.getFechaInicio());
        membresiaCliente.setFechaFin(cotizacion.getFechaFin());

        Map<String, Object> respuesta = new HashMap<>();

        if (MetodoPago.EFECTIVO.equals(metodoPago)) {
            if (montoRecibido == null || montoRecibido.compareTo(total) < 0) {
                throw new RuntimeException("El monto recibido ($" + montoRecibido + ") debe ser mayor o igual al total ($" + total + ")");
            }
            BigDecimal cambio = montoRecibido.subtract(total).setScale(2, RoundingMode.HALF_UP);
            pago.setMontoRecibido(montoRecibido);
            pago.setCambio(cambio);
            pago.setEstatusPago(EstatusPago.PAGADO);

            membresiaCliente.setEstatus(EstatusMembresiaCliente.Activa);
            membresiaCliente = membresiaClienteRepository.save(membresiaCliente);

            pago.setMembresiaCliente(membresiaCliente);
            pago = pagoRepository.save(pago);

            respuesta.put("success", true);
            respuesta.put("mensaje", "Venta registrada exitosamente");
            respuesta.put("idPago", pago.getIdVenta());
            respuesta.put("total", total);
            respuesta.put("cambio", cambio);
            respuesta.put("idMembresiaCliente", membresiaCliente.getIdMembresiaCliente());
            respuesta.put("estatusPago", EstatusPago.PAGADO);

        } else if (MetodoPago.TARJETA.equals(metodoPago) || MetodoPago.TRANSFERENCIA.equals(metodoPago)) {
            pago.setMontoRecibido(total);
            pago.setCambio(BigDecimal.ZERO);
            pago.setEstatusPago(EstatusPago.PAGADO);

            membresiaCliente.setEstatus(EstatusMembresiaCliente.Activa);
            membresiaCliente = membresiaClienteRepository.save(membresiaCliente);

            pago.setMembresiaCliente(membresiaCliente);
            pago = pagoRepository.save(pago);

            respuesta.put("success", true);
            respuesta.put("mensaje", "Venta registrada exitosamente");
            respuesta.put("idPago", pago.getIdVenta());
            respuesta.put("total", total);
            respuesta.put("idMembresiaCliente", membresiaCliente.getIdMembresiaCliente());
            respuesta.put("estatusPago", EstatusPago.PAGADO);

        } else if (MetodoPago.PAYPAL.equals(metodoPago)) {
            membresiaCliente.setEstatus(EstatusMembresiaCliente.Inactiva);
            membresiaCliente = membresiaClienteRepository.save(membresiaCliente);

            pago.setEstatusPago(EstatusPago.PENDIENTE);
            pago.setMembresiaCliente(membresiaCliente);
            pago = pagoRepository.save(pago);

            Map<String, Object> ordenPaypal = paypalService.crearOrden(
                    total.toString(),
                    "Membresía " + membresia.getTipo() + " - " + cliente.getNombre()
            );

            String orderId = (String) ordenPaypal.get("orderId");
            String approveUrl = (String) ordenPaypal.get("approveUrl");

            pago.setReferenciaExterna(orderId);
            pagoRepository.save(pago);

            respuesta.put("success", true);
            respuesta.put("idPago", pago.getIdVenta());
            respuesta.put("orderId", orderId);
            respuesta.put("approveUrl", approveUrl);
            respuesta.put("total", total);
            respuesta.put("estatusPago", EstatusPago.PENDIENTE);
            respuesta.put("idMembresiaCliente", membresiaCliente.getIdMembresiaCliente());
        }

        return respuesta;
    }

    // ==================== 2.4 PAYPAL REAL ====================

    @Transactional
    public Map<String, Object> capturarPaypal(Integer idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + idPago));

        if (!EstatusPago.PENDIENTE.equals(pago.getEstatusPago())) {
            throw new RuntimeException("El pago no está en estatus PENDIENTE");
        }

        String orderId = pago.getReferenciaExterna();
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("El pago no cuenta con una referencia externa de PayPal");
        }

        Map<String, Object> respuesta = new HashMap<>();

        try {
            Map<String, Object> capturaResult = paypalService.capturarOrden(orderId);
            String status = (String) capturaResult.get("status");

            boolean montoCoincide = false;
            Object purchaseUnits = capturaResult.get("purchase_units");
            if (purchaseUnits instanceof List<?> units && !units.isEmpty()) {
                Object unit = units.get(0);
                if (unit instanceof Map<?, ?> unitMap) {
                    Object payments = unitMap.get("payments");
                    if (payments instanceof Map<?, ?> paymentsMap) {
                        Object captures = paymentsMap.get("captures");
                        if (captures instanceof List<?> capturesList && !capturesList.isEmpty()) {
                            Object cap = capturesList.get(0);
                            if (cap instanceof Map<?, ?> capMap) {
                                Object amountObj = capMap.get("amount");
                                if (amountObj instanceof Map<?, ?> amountMap) {
                                    String capturedValue = String.valueOf(amountMap.get("value"));
                                    BigDecimal captured = new BigDecimal(capturedValue).setScale(2, RoundingMode.HALF_UP);
                                    BigDecimal expected = BigDecimal.valueOf(pago.getTotal()).setScale(2, RoundingMode.HALF_UP);
                                    montoCoincide = captured.compareTo(expected) == 0;
                                }
                            }
                        }
                    }
                }
            }

            if ("COMPLETED".equalsIgnoreCase(status) && montoCoincide) {
                pago.setEstatusPago(EstatusPago.PAGADO);
                pago.setMontoRecibido(BigDecimal.valueOf(pago.getTotal()));
                pago.setCambio(BigDecimal.ZERO);
                pagoRepository.save(pago);

                if (pago.getMembresiaCliente() != null) {
                    MembresiaCliente mc = pago.getMembresiaCliente();
                    mc.setEstatus(EstatusMembresiaCliente.Activa);
                    membresiaClienteRepository.save(mc);
                }

                respuesta.put("success", true);
                respuesta.put("mensaje", "Pago con PayPal completado exitosamente");
                respuesta.put("status", "COMPLETED");
                respuesta.put("idPago", pago.getIdVenta());
            } else {
                pago.setEstatusPago(EstatusPago.CANCELADO);
                pagoRepository.save(pago);

                if (pago.getMembresiaCliente() != null) {
                    MembresiaCliente mc = pago.getMembresiaCliente();
                    mc.setEstatus(EstatusMembresiaCliente.Inactiva);
                    membresiaClienteRepository.save(mc);
                }

                respuesta.put("success", false);
                respuesta.put("mensaje", "La captura de PayPal no fue completada correctamente o el monto no coincidió.");
                respuesta.put("status", status != null ? status : "FAILED");
            }

        } catch (Exception e) {
            log.error("Fallo al capturar PayPal para pago {}: {}", idPago, e.getMessage());
            pago.setEstatusPago(EstatusPago.CANCELADO);
            pagoRepository.save(pago);

            if (pago.getMembresiaCliente() != null) {
                MembresiaCliente mc = pago.getMembresiaCliente();
                mc.setEstatus(EstatusMembresiaCliente.Inactiva);
                membresiaClienteRepository.save(mc);
            }

            throw new RuntimeException("Error al capturar la orden en PayPal: " + e.getMessage());
        }

        return respuesta;
    }

    // ==================== 2.5 RENOVAR, CAMBIAR Y CANCELAR ====================

    @Transactional
    public Map<String, Object> renovarMembresia(Long idMembresiaCliente, String idMembresia, Long idPlanPago,
                                                 MetodoPago metodoPago, BigDecimal montoRecibido) {
        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        LocalDate nuevaFechaInicio;
        if (membresiaActual.getFechaFin() != null && !membresiaActual.getFechaFin().isBefore(LocalDate.now())) {
            nuevaFechaInicio = membresiaActual.getFechaFin().plusDays(1);
        } else {
            nuevaFechaInicio = LocalDate.now();
        }

        String folioCliente = membresiaActual.getCliente().getFolioCliente();
        String targetMembresiaId = idMembresia != null ? idMembresia : membresiaActual.getMembresia().getIdMembresia();
        Long targetPlanPagoId = idPlanPago != null ? idPlanPago : (membresiaActual.getPlanPago() != null ? membresiaActual.getPlanPago().getId() : 1L);

        return venderMembresia(folioCliente, targetMembresiaId, targetPlanPagoId, nuevaFechaInicio, metodoPago, montoRecibido);
    }

    @Transactional
    public Map<String, Object> cambiarMembresia(Long idMembresiaCliente, String nuevaIdMembresia,
                                                 MetodoPago metodoPago, BigDecimal montoRecibido) {
        MembresiaCliente actual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía cliente no encontrada: " + idMembresiaCliente));

        if (!EstatusMembresiaCliente.Activa.equals(actual.getEstatus())) {
            throw new RuntimeException("Solo se pueden cambiar membresías con estatus Activa");
        }

        Membresia nueva = membresiaRepository.findById(nuevaIdMembresia)
                .orElseThrow(() -> new RuntimeException("Membresía nueva no encontrada: " + nuevaIdMembresia));

        if (!EstatusCatalogo.Activa.equals(nueva.getEstatus())) {
            throw new RuntimeException("La membresía a la que desea cambiar no está activa");
        }

        LocalDate hoy = LocalDate.now();
        long diasRestantes = actual.getFechaFin() != null && !actual.getFechaFin().isBefore(hoy)
                ? ChronoUnit.DAYS.between(hoy, actual.getFechaFin()) + 1
                : 0;

        int duracionActual = actual.getMembresia().getDuracion() != null && actual.getMembresia().getDuracion() > 0 ? actual.getMembresia().getDuracion() : 30;
        int duracionNueva = nueva.getDuracion() != null && nueva.getDuracion() > 0 ? nueva.getDuracion() : 30;

        double precioDiarioActual = actual.getMembresia().getPrecio() / duracionActual;
        double precioDiarioNuevo = nueva.getPrecio() / duracionNueva;

        double diferenciaPorDia = Math.max(0.0, precioDiarioNuevo - precioDiarioActual);
        BigDecimal totalDiferencia = BigDecimal.valueOf(diferenciaPorDia * diasRestantes).setScale(2, RoundingMode.HALF_UP);

        Recepcionista recepcionista = obtenerRecepcionistaCobro();

        Pago pago = new Pago();
        pago.setCliente(actual.getCliente());
        pago.setRecepcionista(recepcionista);
        pago.setFechaVenta(LocalDateTime.now());
        pago.setCantidad(1);
        pago.setPrecioUnitario(totalDiferencia.doubleValue());
        pago.setTotal(totalDiferencia.doubleValue());
        pago.setMetodoPago(metodoPago);
        pago.setIdMembresia(nuevaIdMembresia);

        // Cancelar membresía actual
        actual.setEstatus(EstatusMembresiaCliente.Cancelada);
        membresiaClienteRepository.save(actual);

        // Crear nueva membresía conservando fechaFin de la actual
        MembresiaCliente nuevaMC = new MembresiaCliente();
        nuevaMC.setCliente(actual.getCliente());
        nuevaMC.setMembresia(nueva);
        nuevaMC.setPlanPago(actual.getPlanPago());
        nuevaMC.setFechaInicio(hoy);
        nuevaMC.setFechaFin(actual.getFechaFin());
        nuevaMC.setEstatus(EstatusMembresiaCliente.Activa);
        nuevaMC = membresiaClienteRepository.save(nuevaMC);

        if (MetodoPago.EFECTIVO.equals(metodoPago)) {
            BigDecimal cambio = BigDecimal.ZERO;
            if (montoRecibido != null && montoRecibido.compareTo(totalDiferencia) >= 0) {
                cambio = montoRecibido.subtract(totalDiferencia);
            }
            pago.setMontoRecibido(montoRecibido != null ? montoRecibido : totalDiferencia);
            pago.setCambio(cambio);
        } else {
            pago.setMontoRecibido(totalDiferencia);
            pago.setCambio(BigDecimal.ZERO);
        }

        pago.setEstatusPago(EstatusPago.PAGADO);
        pago.setMembresiaCliente(nuevaMC);
        pago = pagoRepository.save(pago);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("mensaje", "Cambio de membresía completado exitosamente");
        respuesta.put("totalDiferencia", totalDiferencia);
        respuesta.put("idPago", pago.getIdVenta());
        respuesta.put("nuevaMembresiaCliente", nuevaMC.getIdMembresiaCliente());
        return respuesta;
    }

    @Transactional
    public Map<String, Object> cancelarMembresia(Long idMembresiaCliente, String motivo, String usuarioCancelo) {
        MembresiaCliente membresia = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía cliente no encontrada: " + idMembresiaCliente));

        membresia.setEstatus(EstatusMembresiaCliente.Cancelada);
        membresia.setFechaActualizacion(LocalDateTime.now());
        membresiaClienteRepository.save(membresia);

        log.info("Membresía {} cancelada por {}. Motivo: {}", idMembresiaCliente, usuarioCancelo, motivo);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("mensaje", "Membresía cancelada correctamente. No aplica reembolso.");
        resp.put("idMembresiaCliente", idMembresiaCliente);
        resp.put("canceladoPor", usuarioCancelo);
        resp.put("fechaCancelacion", LocalDateTime.now());
        return resp;
    }

    // ==================== 2.6 VENTA DE PRODUCTOS ====================

    @Transactional
    public Map<String, Object> venderProducto(String folioCliente, String codigoProducto, Integer cantidad,
                                              MetodoPago metodoPago, BigDecimal montoRecibido) {
        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con folio: " + folioCliente));

        // Bloqueo pesimista para evitar sobreventa concurrente
        Producto producto = productoRepository.findByCodigoWithLock(codigoProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigoProducto));

        if (!producto.isActivo()) {
            throw new RuntimeException("El producto no está activo");
        }

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                    ". Solicitado: " + cantidad + ", Disponible: " + producto.getStock());
        }

        // Descontar stock atómicamente
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecio()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);

        Recepcionista recepcionista = obtenerRecepcionistaCobro();

        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setProducto(producto);
        pago.setRecepcionista(recepcionista);
        pago.setFechaVenta(LocalDateTime.now());
        pago.setCantidad(cantidad);
        pago.setPrecioUnitario(precioUnitario.doubleValue());
        pago.setTotal(total.doubleValue());
        pago.setMetodoPago(metodoPago);
        pago.setCodigoProducto(codigoProducto);

        if (MetodoPago.EFECTIVO.equals(metodoPago)) {
            if (montoRecibido == null || montoRecibido.compareTo(total) < 0) {
                throw new RuntimeException("Monto recibido insuficiente para cubrir el total de $" + total);
            }
            pago.setMontoRecibido(montoRecibido);
            pago.setCambio(montoRecibido.subtract(total).setScale(2, RoundingMode.HALF_UP));
        } else {
            pago.setMontoRecibido(total);
            pago.setCambio(BigDecimal.ZERO);
        }

        pago.setEstatusPago(EstatusPago.PAGADO);
        pago = pagoRepository.save(pago);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("mensaje", "Venta de producto completada exitosamente");
        resp.put("idPago", pago.getIdVenta());
        resp.put("total", total);
        resp.put("stockRestante", producto.getStock());
        return resp;
    }
}
