package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import com.example.harmonyGymBack.repository.PlanPagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalculadoraPrecioServiceTest {

    @Mock
    private MembresiaRepository membresiaRepository;

    @Mock
    private PlanPagoRepository planPagoRepository;

    @InjectMocks
    private CalculadoraPrecioService calculadoraPrecioService;

    private Membresia membresiaBasica;
    private Membresia membresiaVip;
    private PlanPago planMensual;
    private PlanPago planTrimestral;
    private PlanPago planSemestral;
    private PlanPago planAnual;

    @BeforeEach
    void setUp() {
        membresiaBasica = new Membresia();
        membresiaBasica.setIdMembresia("MEM001");
        membresiaBasica.setPrecio(350.0);
        membresiaBasica.setDuracion(30);

        membresiaVip = new Membresia();
        membresiaVip.setIdMembresia("MEM003");
        membresiaVip.setPrecio(699.99);
        membresiaVip.setDuracion(30);

        planMensual = new PlanMensual();
        planTrimestral = new PlanTrimestral();
        planSemestral = new PlanSemestral();
        planAnual = new PlanAnual();
    }

    @Test
    @DisplayName("Cálculo Plan Mensual: 1 mes, factor 1.0, sin descuento")
    void testCalculoPlanMensual() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        CalculadoraPrecioService.ResultadoCalculo res = calculadoraPrecioService.calcular(membresiaBasica, planMensual, inicio);

        assertEquals(1, res.getMeses());
        assertEquals(new BigDecimal("350.00"), res.getPrecioBase());
        assertEquals(new BigDecimal("1.0000"), res.getFactorDescuento());
        assertEquals(new BigDecimal("350.00"), res.getTotal());
        assertEquals(LocalDate.of(2026, 1, 1), res.getFechaInicio());
        assertEquals(LocalDate.of(2026, 1, 30), res.getFechaFin()); // 30 - 1 = +29 dias
    }

    @Test
    @DisplayName("Cálculo Plan Trimestral: 3 meses, factor 0.90 (10% descuento)")
    void testCalculoPlanTrimestral() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        CalculadoraPrecioService.ResultadoCalculo res = calculadoraPrecioService.calcular(membresiaBasica, planTrimestral, inicio);

        assertEquals(3, res.getMeses());
        // 350 * 3 = 1050 * 0.90 = 945.00
        assertEquals(new BigDecimal("945.00"), res.getTotal());
        assertEquals(LocalDate.of(2026, 3, 31), res.getFechaFin()); // 1 + 89 dias
    }

    @Test
    @DisplayName("Cálculo Plan Semestral: 6 meses, factor 0.85 (15% descuento)")
    void testCalculoPlanSemestral() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        CalculadoraPrecioService.ResultadoCalculo res = calculadoraPrecioService.calcular(membresiaBasica, planSemestral, inicio);

        assertEquals(6, res.getMeses());
        // 350 * 6 = 2100 * 0.85 = 1785.00
        assertEquals(new BigDecimal("1785.00"), res.getTotal());
    }

    @Test
    @DisplayName("Cálculo Plan Anual con decimales y redondeo HALF_UP")
    void testCalculoPlanAnualRedondeo() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        // Membresia VIP: 699.99, Plan Anual: ceil(365/30) = 13 meses, factor 0.80
        // 699.99 * 13 = 9099.87 * 0.80 = 7279.896 -> redondeo HALF_UP = 7279.90
        CalculadoraPrecioService.ResultadoCalculo res = calculadoraPrecioService.calcular(membresiaVip, planAnual, inicio);

        assertEquals(13, res.getMeses());
        assertEquals(new BigDecimal("7279.90"), res.getTotal());
    }

    @Test
    @DisplayName("Mínimo 1 mes aunque duracionDias sea menor a la duración de membresía")
    void testMinimoUnMes() {
        PlanMensual planCorto = new PlanMensual();
        planCorto.setDuracionDias(15);
        planCorto.setFactorDescuento(1.0);

        CalculadoraPrecioService.ResultadoCalculo res = calculadoraPrecioService.calcular(membresiaBasica, planCorto, LocalDate.now());
        assertEquals(1, res.getMeses());
    }
}
