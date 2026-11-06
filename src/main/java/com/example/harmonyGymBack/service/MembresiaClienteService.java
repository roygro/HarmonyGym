package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.MembresiaCliente;
import com.example.harmonyGymBack.model.PlanPago;
import com.example.harmonyGymBack.model.PlanPersonalizado;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import com.example.harmonyGymBack.model.enums.EstatusMembresiaCliente;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import com.example.harmonyGymBack.repository.PlanPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MembresiaClienteService {

    private final MembresiaClienteRepository membresiaClienteRepository;

    private final ClienteRepository clienteRepository;

    private final MembresiaRepository membresiaRepository;

    private final PlanPagoRepository planPagoRepository;

    public MembresiaClienteService(MembresiaClienteRepository membresiaClienteRepository, ClienteRepository clienteRepository, MembresiaRepository membresiaRepository, PlanPagoRepository planPagoRepository) {
        this.membresiaClienteRepository = membresiaClienteRepository;
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.planPagoRepository = planPagoRepository;
    }


    public MembresiaCliente asignarMembresiaACliente(String folioCliente, String idMembresia, Long idPlanPago, LocalDate fechaInicio) {
        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + folioCliente));

        if (!EstatusPersona.Activo.equals(cliente.getEstatus())) {
            throw new RuntimeException("El cliente no está activo");
        }

        Membresia membresia = membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresia));

        if (!EstatusCatalogo.Activa.equals(membresia.getEstatus())) {
            throw new RuntimeException("La membresía no está activa");
        }

        PlanPago planPago = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + idPlanPago));

        if (!EstatusPersona.Activo.equals(planPago.getEstatus())) {
            throw new RuntimeException("El plan de pago no está activo");
        }

        if (membresiaClienteRepository.existsByClienteFolioClienteAndEstatus(folioCliente, EstatusMembresiaCliente.Activa)) {
            throw new RuntimeException("El cliente ya tiene una membresía activa");
        }

        MembresiaCliente nuevaMembresia = new MembresiaCliente(cliente, membresia, fechaInicio, planPago);
        nuevaMembresia.setEstatus(EstatusMembresiaCliente.Activa);

        return membresiaClienteRepository.save(nuevaMembresia);
    }

    public MembresiaCliente renovarMembresia(Long idMembresiaCliente) {
        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        String folioCliente = membresiaActual.getCliente().getFolioCliente();
        EstatusMembresiaCliente estatusActual = membresiaActual.getEstatus();

        boolean tieneOtraMembresiaActiva = membresiaClienteRepository
                .existsByClienteFolioClienteAndEstatusAndIdMembresiaClienteNot(
                        folioCliente, EstatusMembresiaCliente.Activa, idMembresiaCliente);

        if (tieneOtraMembresiaActiva) {
            throw new RuntimeException("El cliente ya tiene otra membresía activa. Cancele la actual antes de renovar.");
        }

        if (membresiaActual.getPlanPago() == null) {
            PlanPago planPorDefecto = planPagoRepository.findByEstatus(EstatusPersona.Activo).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró un plan de pago por defecto"));
            membresiaActual.setPlanPago(planPorDefecto);
            membresiaClienteRepository.save(membresiaActual);
        }

        if (EstatusMembresiaCliente.Activa.equals(estatusActual)) {
            membresiaActual.setEstatus(EstatusMembresiaCliente.Inactiva);
            membresiaClienteRepository.save(membresiaActual);

            LocalDate nuevaFechaInicio = membresiaActual.getFechaFin().plusDays(1);
            MembresiaCliente nuevaMembresia = new MembresiaCliente(
                    membresiaActual.getCliente(),
                    membresiaActual.getMembresia(),
                    nuevaFechaInicio,
                    membresiaActual.getPlanPago()
            );
            nuevaMembresia.setEstatus(EstatusMembresiaCliente.Activa);
            return membresiaClienteRepository.save(nuevaMembresia);
        } else if (EstatusMembresiaCliente.Expirada.equals(estatusActual) || EstatusMembresiaCliente.Cancelada.equals(estatusActual)) {
            MembresiaCliente membresiaReactivada = new MembresiaCliente(
                    membresiaActual.getCliente(),
                    membresiaActual.getMembresia(),
                    LocalDate.now(),
                    membresiaActual.getPlanPago()
            );
            membresiaReactivada.setEstatus(EstatusMembresiaCliente.Activa);
            return membresiaClienteRepository.save(membresiaReactivada);
        } else if (EstatusMembresiaCliente.Inactiva.equals(estatusActual)) {
            membresiaActual.setEstatus(EstatusMembresiaCliente.Activa);
            if (membresiaActual.getFechaFin().isBefore(LocalDate.now())) {
                LocalDate nuevaFechaFin = membresiaActual.getPlanPago().calcularFechaFin(LocalDate.now());
                membresiaActual.setFechaFin(nuevaFechaFin);
            }
            return membresiaClienteRepository.save(membresiaActual);
        }

        throw new RuntimeException("No se puede renovar una membresía con estatus: " + estatusActual);
    }

    public MembresiaCliente cancelarMembresia(Long idMembresiaCliente) {
        MembresiaCliente membresia = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!EstatusMembresiaCliente.Activa.equals(membresia.getEstatus())) {
            throw new RuntimeException("Solo se pueden cancelar membresías activas");
        }

        membresia.setEstatus(EstatusMembresiaCliente.Cancelada);
        return membresiaClienteRepository.save(membresia);
    }

    public MembresiaCliente obtenerMembresiaActiva(String folioCliente) {
        return membresiaClienteRepository.findByClienteFolioClienteAndEstatus(folioCliente, EstatusMembresiaCliente.Activa)
                .orElseThrow(() -> new RuntimeException("El cliente no tiene membresía activa"));
    }

    public List<MembresiaCliente> obtenerHistorialMembresias(String folioCliente) {
        return membresiaClienteRepository.findByClienteFolioClienteOrderByFechaInicioDesc(folioCliente);
    }

    public List<MembresiaCliente> obtenerTodasMembresiasActivas() {
        return membresiaClienteRepository.findByEstatus(EstatusMembresiaCliente.Activa);
    }

    public List<MembresiaCliente> obtenerMembresiasPorExpirar(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return membresiaClienteRepository.findMembresiasPorExpirar(hoy, fechaLimite);
    }

    public Map<String, Object> verificarAccesoCliente(String folioCliente) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            MembresiaCliente membresia = obtenerMembresiaActiva(folioCliente);

            if (membresia.isExpirada()) {
                membresia.setEstatus(EstatusMembresiaCliente.Expirada);
                membresiaClienteRepository.save(membresia);

                resultado.put("accesoPermitido", false);
                resultado.put("motivo", "Membresía expirada");
            } else {
                resultado.put("accesoPermitido", true);
                resultado.put("membresia", membresia.getMembresia().getTipo());
                resultado.put("planPago", membresia.getPlanPago() != null ? membresia.getPlanPago().getNombre() : null);
                resultado.put("fechaFin", membresia.getFechaFin());
                resultado.put("diasRestantes", java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), membresia.getFechaFin()));
            }

        } catch (RuntimeException e) {
            resultado.put("accesoPermitido", false);
            resultado.put("motivo", "No tiene membresía activa");
        }

        return resultado;
    }

    @Transactional
    public int actualizarMembresiasExpiradas() {
        LocalDate hoy = LocalDate.now();
        return membresiaClienteRepository.actualizarMembresiasExpiradas(hoy);
    }

    public Map<String, Object> obtenerEstadisticasMembresias() {
        Map<String, Object> estadisticas = new HashMap<>();

        Long totalMembresias = membresiaClienteRepository.count();
        Long membresiasActivas = membresiaClienteRepository.countByEstatus(EstatusMembresiaCliente.Activa);
        Long membresiasExpiradas = membresiaClienteRepository.countByEstatus(EstatusMembresiaCliente.Expirada);
        Long membresiasCanceladas = membresiaClienteRepository.countByEstatus(EstatusMembresiaCliente.Cancelada);

        List<MembresiaCliente> porExpirar = obtenerMembresiasPorExpirar(7);

        estadisticas.put("totalMembresias", totalMembresias);
        estadisticas.put("membresiasActivas", membresiasActivas);
        estadisticas.put("membresiasExpiradas", membresiasExpiradas);
        estadisticas.put("membresiasCanceladas", membresiasCanceladas);
        estadisticas.put("membresiasPorExpirar7Dias", porExpirar.size());
        estadisticas.put("porcentajeActivas", totalMembresias > 0 ? (membresiasActivas * 100.0 / totalMembresias) : 0);

        return estadisticas;
    }

    public MembresiaCliente cambiarMembresiaCliente(Long idMembresiaCliente, String nuevaIdMembresia) {
        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!EstatusMembresiaCliente.Activa.equals(membresiaActual.getEstatus())) {
            throw new RuntimeException("Solo se pueden cambiar membresías activas");
        }

        Membresia nuevaMembresia = membresiaRepository.findById(nuevaIdMembresia)
                .orElseThrow(() -> new RuntimeException("Nueva membresía no encontrada: " + nuevaIdMembresia));

        if (!EstatusCatalogo.Activa.equals(nuevaMembresia.getEstatus())) {
            throw new RuntimeException("La nueva membresía no está activa");
        }

        membresiaActual.setEstatus(EstatusMembresiaCliente.Cancelada);

        MembresiaCliente nuevaMembresiaCliente = new MembresiaCliente(
                membresiaActual.getCliente(),
                nuevaMembresia,
                LocalDate.now(),
                membresiaActual.getPlanPago()
        );
        nuevaMembresiaCliente.setEstatus(EstatusMembresiaCliente.Activa);

        return membresiaClienteRepository.save(nuevaMembresiaCliente);
    }

    public List<MembresiaCliente> obtenerTodasLasMembresias() {
        return membresiaClienteRepository.findAll();
    }

    public MembresiaCliente cambiarPlanPago(Long idMembresiaCliente, Long nuevoIdPlanPago) {
        MembresiaCliente membresia = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!EstatusMembresiaCliente.Activa.equals(membresia.getEstatus())) {
            throw new RuntimeException("Solo se pueden cambiar planes de pago en membresías activas");
        }

        PlanPago nuevoPlan = planPagoRepository.findById(nuevoIdPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + nuevoIdPlanPago));

        if (!EstatusPersona.Activo.equals(nuevoPlan.getEstatus())) {
            throw new RuntimeException("El plan de pago no está activo");
        }

        membresia.setPlanPago(nuevoPlan);
        return membresiaClienteRepository.save(membresia);
    }

    public PlanPago crearPlanPago(String nombre, String descripcion, Integer duracionDias, Double factorDescuento) {
        Optional<PlanPago> planExistente = planPagoRepository.findByNombre(nombre);
        if (planExistente.isPresent()) {
            throw new RuntimeException("Ya existe un plan de pago con el nombre: " + nombre);
        }

        PlanPersonalizado planCustom = new PlanPersonalizado();
        planCustom.setNombre(nombre);
        planCustom.setDescripcion(descripcion);
        planCustom.setDuracionDias(duracionDias);
        planCustom.setFactorDescuento(factorDescuento);
        planCustom.setEstatus(EstatusPersona.Activo);

        return planPagoRepository.save(planCustom);
    }

    public List<PlanPago> obtenerPlanesDisponibles() {
        return planPagoRepository.findByEstatus(EstatusPersona.Activo);
    }

    public PlanPago crearPromocion(String nombrePromocion, Object porcentajeDescuento, Integer duracionDias) {
        Double factorDescuento;

        if (porcentajeDescuento instanceof Integer) {
            factorDescuento = 1.0 - (((Integer) porcentajeDescuento) / 100.0);
        } else if (porcentajeDescuento instanceof Double) {
            factorDescuento = 1.0 - (((Double) porcentajeDescuento) / 100.0);
        } else {
            throw new RuntimeException("Tipo de dato no válido para porcentajeDescuento: " + porcentajeDescuento.getClass());
        }

        return crearPlanPago(
                nombrePromocion,
                "Promoción especial: " + porcentajeDescuento + "% de descuento",
                duracionDias,
                factorDescuento
        );
    }

    public List<PlanPago> obtenerPlanesConDescuento() {
        return planPagoRepository.findPlanesConDescuento();
    }

    public PlanPago desactivarPlanPago(Long idPlanPago) {
        PlanPago plan = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + idPlanPago));

        plan.setEstatus(EstatusPersona.Inactivo);
        return planPagoRepository.save(plan);
    }

    public Map<String, Object> obtenerEstadisticasPlanesPago() {
        Map<String, Object> estadisticas = new HashMap<>();

        List<PlanPago> todosPlanes = planPagoRepository.findAll();
        List<PlanPago> planesActivos = planPagoRepository.findByEstatus(EstatusPersona.Activo);
        List<PlanPago> planesConDescuento = planPagoRepository.findPlanesConDescuento();

        estadisticas.put("totalPlanes", todosPlanes.size());
        estadisticas.put("planesActivos", planesActivos.size());
        estadisticas.put("planesConDescuento", planesConDescuento.size());
        estadisticas.put("planesPopulares", obtenerPlanesPopulares());

        return estadisticas;
    }

    private List<Map<String, Object>> obtenerPlanesPopulares() {
        List<PlanPago> planesActivos = planPagoRepository.findByEstatus(EstatusPersona.Activo);

        return planesActivos.stream()
                .map(plan -> {
                    Map<String, Object> planInfo = new HashMap<>();
                    planInfo.put("id", plan.getId());
                    planInfo.put("nombre", plan.getNombre());
                    planInfo.put("descripcion", plan.getDescripcion());
                    planInfo.put("factorDescuento", plan.getFactorDescuento());
                    return planInfo;
                })
                .toList();
    }
}
