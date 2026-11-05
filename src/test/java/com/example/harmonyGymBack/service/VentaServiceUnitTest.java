package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.*;
import com.example.harmonyGymBack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceUnitTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private MembresiaRepository membresiaRepository;
    @Mock
    private PlanPagoRepository planPagoRepository;
    @Mock
    private MembresiaClienteRepository membresiaClienteRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private RecepcionistaRepository recepcionistaRepository;
    @Mock
    private CalculadoraPrecioService calculadoraPrecioService;
    @Mock
    private PaypalService paypalService;

    @InjectMocks
    private VentaService ventaService;

    private Cliente cliente;
    private Membresia membresia;
    private PlanPago plan;
    private Recepcionista recepcionista;
    private CalculadoraPrecioService.ResultadoCalculo cotizacion;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setFolioCliente("CLI001");
        cliente.setNombre("Juan Pérez");
        cliente.setEstatus(EstatusPersona.Activo);

        membresia = new Membresia();
        membresia.setIdMembresia("MEM001");
        membresia.setTipo(TipoMembresia.Básica);
        membresia.setPrecio(350.0);
        membresia.setEstatus(EstatusCatalogo.Activa);

        plan = new PlanMensual();
        plan.setId(1L);
        plan.setNombre("Plan Mensual");
        plan.setDuracionDias(30);
        plan.setEstatus(EstatusPersona.Activo);

        recepcionista = new Recepcionista();
        recepcionista.setIdRecepcionista("REC001");
        recepcionista.setNombre("Recepcionista Demo");
        recepcionista.setEstatus(EstatusPersona.Activo);

        cotizacion = new CalculadoraPrecioService.ResultadoCalculo(
                new BigDecimal("350.00"),
                1,
                new BigDecimal("1.0000"),
                new BigDecimal("350.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(29)
        );
    }

    @Test
    @DisplayName("Cobro en efectivo con cambio correcto")
    void testVentaMembresiaEfectivoConCambioCorrecto() {
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(membresiaRepository.findById("MEM001")).thenReturn(Optional.of(membresia));
        when(planPagoRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.empty());
        when(calculadoraPrecioService.calcular(any(), any(), any())).thenReturn(cotizacion);
        when(recepcionistaRepository.findByEstatus(EstatusPersona.Activo)).thenReturn(List.of(recepcionista));

        when(membresiaClienteRepository.save(any(MembresiaCliente.class))).thenAnswer(invocation -> {
            MembresiaCliente mc = invocation.getArgument(0);
            mc.setIdMembresiaCliente(10L);
            return mc;
        });

        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago p = invocation.getArgument(0);
            p.setIdVenta(100);
            return p;
        });

        // Total $350.00, monto recibido $500.00 -> cambio debe ser $150.00
        BigDecimal montoRecibido = new BigDecimal("500.00");
        Map<String, Object> resp = ventaService.venderMembresia(
                "CLI001", "MEM001", 1L, LocalDate.now(), MetodoPago.EFECTIVO, montoRecibido
        );

        assertTrue((Boolean) resp.get("success"));
        assertEquals(new BigDecimal("350.00"), resp.get("total"));
        assertEquals(new BigDecimal("150.00"), resp.get("cambio"));
        assertEquals(EstatusPago.PAGADO, resp.get("estatusPago"));

        verify(pagoRepository).save(argThat(p ->
                p.getEstatusPago() == EstatusPago.PAGADO &&
                p.getMontoRecibido().compareTo(montoRecibido) == 0 &&
                p.getCambio().compareTo(new BigDecimal("150.00")) == 0
        ));
    }

    @Test
    @DisplayName("Cobro insuficiente en efectivo es rechazado")
    void testVentaMembresiaEfectivoMontoInsuficienteRechazado() {
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(membresiaRepository.findById("MEM001")).thenReturn(Optional.of(membresia));
        when(planPagoRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.empty());
        when(calculadoraPrecioService.calcular(any(), any(), any())).thenReturn(cotizacion);
        when(recepcionistaRepository.findByEstatus(EstatusPersona.Activo)).thenReturn(List.of(recepcionista));

        // Total $350.00, monto recibido $200.00 (insuficiente)
        BigDecimal montoInsuficiente = new BigDecimal("200.00");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ventaService.venderMembresia("CLI001", "MEM001", 1L, LocalDate.now(), MetodoPago.EFECTIVO, montoInsuficiente)
        );

        assertTrue(ex.getMessage().contains("debe ser mayor o igual al total"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Creación de orden PayPal devuelve approveUrl y deja pago PENDIENTE")
    void testVentaMembresiaCreacionOrdenPaypal() {
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(membresiaRepository.findById("MEM001")).thenReturn(Optional.of(membresia));
        when(planPagoRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.empty());
        when(calculadoraPrecioService.calcular(any(), any(), any())).thenReturn(cotizacion);
        when(recepcionistaRepository.findByEstatus(EstatusPersona.Activo)).thenReturn(List.of(recepcionista));

        when(membresiaClienteRepository.save(any(MembresiaCliente.class))).thenAnswer(invocation -> {
            MembresiaCliente mc = invocation.getArgument(0);
            mc.setIdMembresiaCliente(20L);
            return mc;
        });

        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago p = invocation.getArgument(0);
            p.setIdVenta(200);
            return p;
        });

        Map<String, Object> paypalResp = new HashMap<>();
        paypalResp.put("orderId", "PAYPAL-ORDER-123");
        paypalResp.put("approveUrl", "https://www.sandbox.paypal.com/checkoutnow?token=PAYPAL-ORDER-123");
        when(paypalService.crearOrden(anyString(), anyString())).thenReturn(paypalResp);

        Map<String, Object> resp = ventaService.venderMembresia(
                "CLI001", "MEM001", 1L, LocalDate.now(), MetodoPago.PAYPAL, null
        );

        assertTrue((Boolean) resp.get("success"));
        assertEquals("PAYPAL-ORDER-123", resp.get("orderId"));
        assertEquals("https://www.sandbox.paypal.com/checkoutnow?token=PAYPAL-ORDER-123", resp.get("approveUrl"));
        assertEquals(EstatusPago.PENDIENTE, resp.get("estatusPago"));

        // Se verifica que la membresía quedó inactiva temporalmente esperando el webhook o captura
        verify(membresiaClienteRepository).save(argThat(mc -> mc.getEstatus() == EstatusMembresiaCliente.Inactiva));
    }
}
