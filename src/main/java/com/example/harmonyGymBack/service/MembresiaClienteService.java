package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.MembresiaCliente;
import com.example.harmonyGymBack.model.PlanPago;
import com.example.harmonyGymBack.model.PlanPersonalizado;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import com.example.harmonyGymBack.repository.PlanPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MembresiaClienteRepository membresiaClienteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    // ✅ NUEVO: Repository para PlanPago
    @Autowired
    private PlanPagoRepository planPagoRepository;

    // ==================== ASIGNAR MEMBRESÍA A CLIENTE ====================

    // ✅ MODIFICADO: Ahora recibe idPlanPago
    public MembresiaCliente asignarMembresiaACliente(String folioCliente, String idMembresia, Long idPlanPago, LocalDate fechaInicio) {
        System.out.println("🚀 Asignando membresía con plan de pago al cliente: " + folioCliente);

        // Verificar que el cliente existe y está activo
        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + folioCliente));

        if (!"Activo".equals(cliente.getEstatus())) {
            throw new RuntimeException("El cliente no está activo");
        }

        // Verificar que la membresía existe y está activa
        Membresia membresia = membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresia));

        if (!"Activa".equals(membresia.getEstatus())) {
            throw new RuntimeException("La membresía no está activa");
        }

        // ✅ NUEVO: Verificar que el plan de pago existe y está activo
        PlanPago planPago = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + idPlanPago));

        if (!"Activo".equals(planPago.getEstatus())) {
            throw new RuntimeException("El plan de pago no está activo");
        }

        // Verificar que el cliente no tenga membresía activa
        if (membresiaClienteRepository.existsByClienteFolioClienteAndEstatus(folioCliente, "Activa")) {
            throw new RuntimeException("El cliente ya tiene una membresía activa");
        }

        // ✅ MODIFICADO: Crear la nueva membresía con plan de pago
        MembresiaCliente nuevaMembresia = new MembresiaCliente(cliente, membresia, fechaInicio, planPago);

        MembresiaCliente membresiaGuardada = membresiaClienteRepository.save(nuevaMembresia);

        // ✅ NUEVO: Calcular precio final con descuento
        double precioFinal = nuevaMembresia.calcularPrecioFinal();
        System.out.println("✅ Membresía asignada exitosamente: " + membresiaGuardada.getIdMembresiaCliente());
        System.out.println("💰 Precio final con descuento: $" + precioFinal);

        return membresiaGuardada;
    }

    // ==================== RENOVAR MEMBRESÍA ====================

    public MembresiaCliente renovarMembresia(Long idMembresiaCliente) {
        System.out.println("🔄 Renovando membresía: " + idMembresiaCliente);

        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        String folioCliente = membresiaActual.getCliente().getFolioCliente();
        String estatusActual = membresiaActual.getEstatus();

        System.out.println("📊 Estatus actual: " + estatusActual + ", Cliente: " + folioCliente);

        // Verificar si el cliente ya tiene otra membresía activa (excluyendo esta)
        boolean tieneOtraMembresiaActiva = membresiaClienteRepository
                .existsByClienteFolioClienteAndEstatusAndIdMembresiaClienteNot(
                        folioCliente, "Activa", idMembresiaCliente);

        if (tieneOtraMembresiaActiva) {
            throw new RuntimeException("El cliente ya tiene otra membresía activa. Cancele la actual antes de renovar.");
        }

        // ✅ NUEVO: Verificar que la membresía tenga un plan de pago
        if (membresiaActual.getPlanPago() == null) {
            System.out.println("⚠️  Membresía sin plan de pago, asignando plan por defecto");

            // Buscar un plan activo por defecto (por ejemplo, plan mensual sin descuento)
            PlanPago planPorDefecto = planPagoRepository.findByNombre("Plan Mensual Sin Dto")
                    .orElseThrow(() -> new RuntimeException("No se encontró un plan de pago por defecto"));

            membresiaActual.setPlanPago(planPorDefecto);
            membresiaClienteRepository.save(membresiaActual);
            System.out.println("✅ Plan por defecto asignado: " + planPorDefecto.getNombre());
        }

        // Lógica según el estatus actual
        switch (estatusActual) {
            case "Activa":
                System.out.println("🔵 Renovando membresía ACTIVA");
                // Marcar la membresía actual como inactiva y crear nueva
                membresiaActual.setEstatus("Inactiva");
                membresiaClienteRepository.save(membresiaActual);

                // ✅ CORREGIDO: Verificar que planPago no sea null
                LocalDate nuevaFechaInicio = membresiaActual.getFechaFin().plusDays(1);
                MembresiaCliente nuevaMembresia = new MembresiaCliente(
                        membresiaActual.getCliente(),
                        membresiaActual.getMembresia(),
                        nuevaFechaInicio,
                        membresiaActual.getPlanPago() // ✅ Ahora seguro que no es null
                );
                return membresiaClienteRepository.save(nuevaMembresia);

            case "Expirada":
            case "Cancelada":
                System.out.println("🟡 Reactivando membresía " + estatusActual);
                // ✅ CORREGIDO: Verificar que planPago no sea null
                MembresiaCliente membresiaReactivada = new MembresiaCliente(
                        membresiaActual.getCliente(),
                        membresiaActual.getMembresia(),
                        LocalDate.now(),
                        membresiaActual.getPlanPago() // ✅ Ahora seguro que no es null
                );
                return membresiaClienteRepository.save(membresiaReactivada);

            case "Inactiva":
                System.out.println("🟠 Reactivando membresía INACTIVA");
                // Reactivar la membresía existente
                membresiaActual.setEstatus("Activa");
                // Si la fecha fin ya pasó, extenderla usando el plan de pago
                if (membresiaActual.getFechaFin().isBefore(LocalDate.now())) {
                    // ✅ CORREGIDO: Verificar que planPago no sea null antes de usarlo
                    LocalDate nuevaFechaFin = membresiaActual.getPlanPago().calcularFechaFin(LocalDate.now());
                    membresiaActual.setFechaFin(nuevaFechaFin);
                    System.out.println("📅 Extendiendo fecha fin a: " + nuevaFechaFin);
                }
                return membresiaClienteRepository.save(membresiaActual);

            default:
                throw new RuntimeException("No se puede renovar una membresía con estatus: " + estatusActual);
        }
    }
    // ==================== CANCELAR MEMBRESÍA ====================

    public MembresiaCliente cancelarMembresia(Long idMembresiaCliente) {
        System.out.println("❌ Cancelando membresía: " + idMembresiaCliente);

        MembresiaCliente membresia = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!"Activa".equals(membresia.getEstatus())) {
            throw new RuntimeException("Solo se pueden cancelar membresías activas");
        }

        membresia.setEstatus("Cancelada");
        MembresiaCliente membresiaCancelada = membresiaClienteRepository.save(membresia);

        System.out.println("✅ Membresía cancelada: " + idMembresiaCliente);
        return membresiaCancelada;
    }

    // ==================== CONSULTAS ====================

    public MembresiaCliente obtenerMembresiaActiva(String folioCliente) {
        return membresiaClienteRepository.findByClienteFolioClienteAndEstatus(folioCliente, "Activa")
                .orElseThrow(() -> new RuntimeException("El cliente no tiene membresía activa"));
    }

    public List<MembresiaCliente> obtenerHistorialMembresias(String folioCliente) {
        return membresiaClienteRepository.findByClienteFolioClienteOrderByFechaInicioDesc(folioCliente);
    }

    public List<MembresiaCliente> obtenerTodasMembresiasActivas() {
        return membresiaClienteRepository.findByEstatus("Activa");
    }

    public List<MembresiaCliente> obtenerMembresiasPorExpirar(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return membresiaClienteRepository.findMembresiasPorExpirar(hoy, fechaLimite);
    }

    // ==================== VERIFICAR ACCESO ====================

    public Map<String, Object> verificarAccesoCliente(String folioCliente) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            MembresiaCliente membresia = obtenerMembresiaActiva(folioCliente);

            if (membresia.isExpirada()) {
                // Actualizar automáticamente a expirada
                membresia.setEstatus("Expirada");
                membresiaClienteRepository.save(membresia);

                resultado.put("accesoPermitido", false);
                resultado.put("motivo", "Membresía expirada");
            } else {
                resultado.put("accesoPermitido", true);
                resultado.put("membresia", membresia.getMembresia().getTipo());
                resultado.put("planPago", membresia.getPlanPago().getNombre()); // ✅ NUEVO: Incluir info del plan
                resultado.put("fechaFin", membresia.getFechaFin());
                resultado.put("diasRestantes", java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), membresia.getFechaFin()));
            }

        } catch (RuntimeException e) {
            resultado.put("accesoPermitido", false);
            resultado.put("motivo", "No tiene membresía activa");
        }

        return resultado;
    }

    // ==================== ACTUALIZAR MEMBRESÍAS EXPIRADAS ====================

    @Transactional
    public int actualizarMembresiasExpiradas() {
        LocalDate hoy = LocalDate.now();
        int actualizadas = membresiaClienteRepository.actualizarMembresiasExpiradas(hoy);
        System.out.println("🔄 Membresías expiradas actualizadas: " + actualizadas);
        return actualizadas;
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasMembresias() {
        Map<String, Object> estadisticas = new HashMap<>();

        Long totalMembresias = membresiaClienteRepository.count();
        Long membresiasActivas = membresiaClienteRepository.countByEstatus("Activa");
        Long membresiasExpiradas = membresiaClienteRepository.countByEstatus("Expirada");
        Long membresiasCanceladas = membresiaClienteRepository.countByEstatus("Cancelada");

        List<MembresiaCliente> porExpirar = obtenerMembresiasPorExpirar(7);

        estadisticas.put("totalMembresias", totalMembresias);
        estadisticas.put("membresiasActivas", membresiasActivas);
        estadisticas.put("membresiasExpiradas", membresiasExpiradas);
        estadisticas.put("membresiasCanceladas", membresiasCanceladas);
        estadisticas.put("membresiasPorExpirar7Dias", porExpirar.size());
        estadisticas.put("porcentajeActivas", totalMembresias > 0 ? (membresiasActivas * 100.0 / totalMembresias) : 0);

        return estadisticas;
    }

    // ==================== CAMBIAR MEMBRESÍA ====================

    public MembresiaCliente cambiarMembresiaCliente(Long idMembresiaCliente, String nuevaIdMembresia) {
        System.out.println("🔄 Cambiando membresía: " + idMembresiaCliente);

        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!"Activa".equals(membresiaActual.getEstatus())) {
            throw new RuntimeException("Solo se pueden cambiar membresías activas");
        }

        Membresia nuevaMembresia = membresiaRepository.findById(nuevaIdMembresia)
                .orElseThrow(() -> new RuntimeException("Nueva membresía no encontrada: " + nuevaIdMembresia));

        if (!"Activa".equals(nuevaMembresia.getEstatus())) {
            throw new RuntimeException("La nueva membresía no está activa");
        }

        // Cancelar membresía actual
        membresiaActual.setEstatus("Cancelada");

        // ✅ MODIFICADO: Crear nueva membresía manteniendo el mismo plan de pago
        MembresiaCliente nuevaMembresiaCliente = new MembresiaCliente(
                membresiaActual.getCliente(),
                nuevaMembresia,
                LocalDate.now(),
                membresiaActual.getPlanPago() // ✅ Mantener el plan de pago actual

        );

        MembresiaCliente membresiaCambiada = membresiaClienteRepository.save(nuevaMembresiaCliente);
        System.out.println("✅ Membresía cambiada exitosamente: " + membresiaCambiada.getIdMembresiaCliente());

        return membresiaCambiada;
    }

    public List<MembresiaCliente> obtenerTodasLasMembresias() {
        return membresiaClienteRepository.findAll();
    }

    // ==================== MÉTODOS NUEVOS PARA PLANES DE PAGO ====================

    /**
     * ✅ NUEVO: Cambiar el plan de pago de una membresía existente
     */
    public MembresiaCliente cambiarPlanPago(Long idMembresiaCliente, Long nuevoIdPlanPago) {
        System.out.println("🔄 Cambiando plan de pago para membresía: " + idMembresiaCliente);

        MembresiaCliente membresia = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!"Activa".equals(membresia.getEstatus())) {
            throw new RuntimeException("Solo se pueden cambiar planes de pago en membresías activas");
        }

        PlanPago nuevoPlan = planPagoRepository.findById(nuevoIdPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + nuevoIdPlanPago));

        if (!"Activo".equals(nuevoPlan.getEstatus())) {
            throw new RuntimeException("El plan de pago no está activo");
        }

        // Cambiar el plan de pago
        membresia.setPlanPago(nuevoPlan);

        MembresiaCliente membresiaActualizada = membresiaClienteRepository.save(membresia);

        double nuevoPrecio = membresiaActualizada.calcularPrecioFinal();
        System.out.println("✅ Plan de pago cambiado. Nuevo precio: $" + nuevoPrecio);

        return membresiaActualizada;
    }

    /**
     * ✅ NUEVO: Crear un plan de pago personalizado
     */
    public PlanPago crearPlanPago(String nombre, String descripcion, Integer duracionDias, Double factorDescuento) {
        System.out.println("➕ Creando nuevo plan de pago: " + nombre);

        // Verificar si ya existe un plan con el mismo nombre
        Optional<PlanPago> planExistente = planPagoRepository.findByNombre(nombre);
        if (planExistente.isPresent()) {
            throw new RuntimeException("Ya existe un plan de pago con el nombre: " + nombre);
        }

        // Crear plan personalizado
        PlanPersonalizado planCustom = new PlanPersonalizado();
        planCustom.setNombre(nombre);
        planCustom.setDescripcion(descripcion);
        planCustom.setDuracionDias(duracionDias);
        planCustom.setFactorDescuento(factorDescuento);

        PlanPago planGuardado = planPagoRepository.save(planCustom);
        System.out.println("✅ Plan de pago creado exitosamente: " + planGuardado.getId());

        return planGuardado;
    }

    /**
     * ✅ NUEVO: Obtener todos los planes de pago disponibles
     */
    public List<PlanPago> obtenerPlanesDisponibles() {
        return planPagoRepository.findByEstatus("Activo");
    }

    /**
     * ✅ NUEVO: Crear una promoción especial con descuento
     */
    public PlanPago crearPromocion(String nombrePromocion, Object porcentajeDescuento, Integer duracionDias) {
        Double factorDescuento;

        // ✅ MANEJAR DIFERENTES TIPOS DE DATOS
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
    /**
     * ✅ NUEVO: Obtener planes con descuento
     */
    public List<PlanPago> obtenerPlanesConDescuento() {
        return planPagoRepository.findPlanesConDescuento();
    }

    /**
     * ✅ NUEVO: Desactivar un plan de pago
     */
    public PlanPago desactivarPlanPago(Long idPlanPago) {
        PlanPago plan = planPagoRepository.findById(idPlanPago)
                .orElseThrow(() -> new RuntimeException("Plan de pago no encontrado: " + idPlanPago));

        plan.setEstatus("Inactivo");
        return planPagoRepository.save(plan);
    }

    /**
     * ✅ NUEVO: Obtener estadísticas de planes de pago
     */
    public Map<String, Object> obtenerEstadisticasPlanesPago() {
        Map<String, Object> estadisticas = new HashMap<>();

        List<PlanPago> todosPlanes = planPagoRepository.findAll();
        List<PlanPago> planesActivos = planPagoRepository.findByEstatus("Activo");
        List<PlanPago> planesConDescuento = planPagoRepository.findPlanesConDescuento();

        estadisticas.put("totalPlanes", todosPlanes.size());
        estadisticas.put("planesActivos", planesActivos.size());
        estadisticas.put("planesConDescuento", planesConDescuento.size());
        estadisticas.put("planesPopulares", obtenerPlanesPopulares());

        return estadisticas;
    }

    /**
     * ✅ NUEVO: Obtener los planes más populares (más utilizados)
     */
    private List<Map<String, Object>> obtenerPlanesPopulares() {
        // Esta es una implementación básica - puedes mejorarla según tus necesidades
        List<PlanPago> planesActivos = planPagoRepository.findByEstatus("Activo");

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