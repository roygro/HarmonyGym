package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.MembresiaCliente;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaRepository;
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

    // ==================== ASIGNAR MEMBRESÍA A CLIENTE ====================

    public MembresiaCliente asignarMembresiaACliente(String folioCliente, String idMembresia, LocalDate fechaInicio) {
        System.out.println("🚀 Asignando membresía al cliente: " + folioCliente);

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

        // Verificar que el cliente no tenga membresía activa
        if (membresiaClienteRepository.existsByClienteFolioClienteAndEstatus(folioCliente, "Activa")) {
            throw new RuntimeException("El cliente ya tiene una membresía activa");
        }

        // Crear la nueva membresía para el cliente
        MembresiaCliente nuevaMembresia = new MembresiaCliente(cliente, membresia, fechaInicio);

        MembresiaCliente membresiaGuardada = membresiaClienteRepository.save(nuevaMembresia);
        System.out.println("✅ Membresía asignada exitosamente: " + membresiaGuardada.getIdMembresiaCliente());

        return membresiaGuardada;
    }

    // ==================== RENOVAR MEMBRESÍA ====================

    public MembresiaCliente renovarMembresia(Long idMembresiaCliente) {
        System.out.println("🔄 Renovando membresía: " + idMembresiaCliente);

        MembresiaCliente membresiaActual = membresiaClienteRepository.findById(idMembresiaCliente)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada: " + idMembresiaCliente));

        if (!"Activa".equals(membresiaActual.getEstatus())) {
            throw new RuntimeException("Solo se pueden renovar membresías activas");
        }

        // Marcar la membresía actual como inactiva
        membresiaActual.setEstatus("Inactiva");

        // Crear nueva membresía comenzando al día siguiente de la fecha fin
        LocalDate nuevaFechaInicio = membresiaActual.getFechaFin().plusDays(1);
        MembresiaCliente nuevaMembresia = new MembresiaCliente(
                membresiaActual.getCliente(),
                membresiaActual.getMembresia(),
                nuevaFechaInicio
        );

        MembresiaCliente membresiaRenovada = membresiaClienteRepository.save(nuevaMembresia);
        System.out.println("✅ Membresía renovada exitosamente: " + membresiaRenovada.getIdMembresiaCliente());

        return membresiaRenovada;
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
        Long membresiasInactivas = membresiaClienteRepository.countByEstatus("Inactiva");
        Long membresiasExpiradas = membresiaClienteRepository.countByEstatus("Expirada");
        Long membresiasCanceladas = membresiaClienteRepository.countByEstatus("Cancelada");

        List<MembresiaCliente> porExpirar = obtenerMembresiasPorExpirar(7);

        estadisticas.put("totalMembresias", totalMembresias);
        estadisticas.put("membresiasActivas", membresiasActivas);
        estadisticas.put("membresiasInactivas", membresiasInactivas);
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
        membresiaActual.setEstatus("Inactiva");

        // Crear nueva membresía con la nueva configuración
        MembresiaCliente nuevaMembresiaCliente = new MembresiaCliente(
                membresiaActual.getCliente(),
                nuevaMembresia,
                LocalDate.now()
        );

        MembresiaCliente membresiaCambiada = membresiaClienteRepository.save(nuevaMembresiaCliente);
        System.out.println("✅ Membresía cambiada exitosamente: " + membresiaCambiada.getIdMembresiaCliente());

        return membresiaCambiada;
    }
}