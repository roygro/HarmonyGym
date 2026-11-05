package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.PlanPago;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import com.example.harmonyGymBack.repository.PlanPagoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalculadoraPrecioService {

    private final MembresiaRepository membresiaRepository;

    private final PlanPagoRepository planPagoRepository;

    public CalculadoraPrecioService(MembresiaRepository membresiaRepository, PlanPagoRepository planPagoRepository) {
        this.membresiaRepository = membresiaRepository;
        this.planPagoRepository = planPagoRepository;
    }


    public static class ResultadoCalculo {
        private BigDecimal precioBase;
        private int meses;
        private BigDecimal factorDescuento;
        private BigDecimal total;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;

        public ResultadoCalculo(BigDecimal precioBase, int meses, BigDecimal factorDescuento, BigDecimal total, LocalDate fechaInicio, LocalDate fechaFin) {
            this.precioBase = precioBase;
            this.meses = meses;
            this.factorDescuento = factorDescuento;
            this.total = total;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
        }

        public BigDecimal getPrecioBase() { return precioBase; }
        public int getMeses() { return meses; }
        public BigDecimal getFactorDescuento() { return factorDescuento; }
        public BigDecimal getTotal() { return total; }
        public LocalDate getFechaInicio() { return fechaInicio; }
        public LocalDate getFechaFin() { return fechaFin; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("precioBase", precioBase);
            map.put("meses", meses);
            map.put("factorDescuento", factorDescuento);
            map.put("total", total);
            map.put("fechaInicio", fechaInicio);
            map.put("fechaFin", fechaFin);
            return map;
        }
    }

    public ResultadoCalculo calcular(Membresia membresia, PlanPago plan, LocalDate fechaInicio) {
        if (membresia == null || plan == null) {
            throw new IllegalArgumentException("Membresía y PlanPago son requeridos para el cálculo de precio.");
        }
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }

        int duracionMembresia = membresia.getDuracion() != null && membresia.getDuracion() > 0 ? membresia.getDuracion() : 30;
        int duracionPlan = plan.getDuracionDias() != null && plan.getDuracionDias() > 0 ? plan.getDuracionDias() : 30;

        int meses = Math.max(1, (int) Math.ceil((double) duracionPlan / duracionMembresia));

        BigDecimal precioBase = BigDecimal.valueOf(membresia.getPrecio() != null ? membresia.getPrecio() : 0.0)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal factorDescuento = BigDecimal.valueOf(plan.getFactorDescuento() != null ? plan.getFactorDescuento() : 1.0)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal total = precioBase
                .multiply(BigDecimal.valueOf(meses))
                .multiply(factorDescuento)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate fechaFin = fechaInicio.plusDays(duracionPlan - 1);

        return new ResultadoCalculo(precioBase, meses, factorDescuento, total, fechaInicio, fechaFin);
    }

    public ResultadoCalculo cotizar(String idMembresia, Long idPlanPago, LocalDate fechaInicio) {
        Membresia membresia = membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada con ID: " + idMembresia));
        PlanPago plan = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado con ID: " + idPlanPago));

        return calcular(membresia, plan, fechaInicio != null ? fechaInicio : LocalDate.now());
    }
}
