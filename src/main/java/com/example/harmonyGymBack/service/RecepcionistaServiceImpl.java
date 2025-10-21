package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.repository.RecepcionistaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecepcionistaServiceImpl {

    @Autowired
    private RecepcionistaRepository recepcionistaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== GENERACIÓN AUTOMÁTICA DE ID ====================

    private String generarIdRecepcionista() {
        try {
            System.out.println("🔍 Buscando último ID de recepcionista...");

            List<Recepcionista> todosRecepcionistas = recepcionistaRepository.findAll();

            if (todosRecepcionistas.isEmpty()) {
                System.out.println("✅ No hay recepcionistas, empezando con REC001");
                return "REC001";
            }

            String ultimoId = null;
            int maxNumero = 0;

            for (Recepcionista recepcionista : todosRecepcionistas) {
                String id = recepcionista.getIdRecepcionista();
                if (id != null && id.startsWith("REC")) {
                    try {
                        String numeroStr = id.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                            ultimoId = id;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠ ID con formato inválido: " + id);
                    }
                }
            }

            if (ultimoId == null) {
                System.out.println("✅ No se encontraron IDs válidos, empezando con REC001");
                return "REC001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoId = String.format("REC%03d", nuevoNumero);

            System.out.println("📊 Último ID encontrado: " + ultimoId);
            System.out.println("🎯 Nuevo ID generado: " + nuevoId);

            return nuevoId;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar ID: " + e.getMessage());
            e.printStackTrace();

            long totalRecepcionistas = recepcionistaRepository.count();
            String idFallback = String.format("REC%03d", totalRecepcionistas + 1);
            System.out.println("🔄 Usando fallback: " + idFallback);
            return idFallback;
        }
    }

    // ==================== CREAR NUEVO RECEPCIONISTA ====================

    @Transactional
    public Recepcionista crearRecepcionista(String nombre, String telefono, String email,
                                            String fechaContratacion, String estatus) {
        System.out.println("🚀 Iniciando creación de recepcionista...");

        String idGenerado = generarIdRecepcionista();

        Recepcionista recepcionista = new Recepcionista();
        recepcionista.setIdRecepcionista(idGenerado);
        recepcionista.setNombre(nombre);
        recepcionista.setTelefono(telefono);
        recepcionista.setEmail(email);
        recepcionista.setEstatus(estatus != null ? estatus : "Activo");
        recepcionista.setFechaRegistro(LocalDateTime.now());

        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            recepcionista.setFechaContratacion(LocalDate.parse(fechaContratacion));
        } else {
            recepcionista.setFechaContratacion(LocalDate.now());
        }

        // Validar email único
        if (recepcionista.getEmail() != null && recepcionistaRepository.findByEmail(recepcionista.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado por otro recepcionista");
        }

        // Validar teléfono único
        if (recepcionista.getTelefono() != null && recepcionistaRepository.findByTelefono(recepcionista.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado por otro recepcionista");
        }

        Recepcionista recepcionistaGuardado = recepcionistaRepository.save(recepcionista);
        System.out.println("✅ Recepcionista creado exitosamente: " + idGenerado);

        return recepcionistaGuardado;
    }

    // ==================== ACTUALIZAR RECEPCIONISTA ====================

    @Transactional
    public Recepcionista actualizarRecepcionista(String idRecepcionista, String nombre, String telefono,
                                                 String email, String fechaContratacion, String estatus) {
        System.out.println("✏ Actualizando recepcionista: " + idRecepcionista);

        Recepcionista recepcionistaExistente = obtenerRecepcionistaPorId(idRecepcionista);

        if (nombre != null) recepcionistaExistente.setNombre(nombre);
        if (telefono != null) recepcionistaExistente.setTelefono(telefono);
        if (email != null) recepcionistaExistente.setEmail(email);
        if (estatus != null) recepcionistaExistente.setEstatus(estatus);

        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            recepcionistaExistente.setFechaContratacion(LocalDate.parse(fechaContratacion));
        }

        // Validar email único excluyendo el actual
        if (email != null && recepcionistaRepository.existsByEmailAndNotId(email, idRecepcionista)) {
            throw new RuntimeException("El email ya está registrado por otro recepcionista");
        }

        // Validar teléfono único excluyendo el actual
        if (telefono != null && recepcionistaRepository.existsByTelefonoAndNotId(telefono, idRecepcionista)) {
            throw new RuntimeException("El teléfono ya está registrado por otro recepcionista");
        }

        Recepcionista recepcionistaActualizado = recepcionistaRepository.save(recepcionistaExistente);
        System.out.println("✅ Recepcionista actualizado: " + idRecepcionista);

        return recepcionistaActualizado;
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Recepcionista crearRecepcionista(Recepcionista recepcionista) {
        System.out.println("🚀 Iniciando creación de recepcionista...");

        String idGenerado = generarIdRecepcionista();
        recepcionista.setIdRecepcionista(idGenerado);

        System.out.println("📝 ID asignado: " + idGenerado);

        if (recepcionista.getFechaRegistro() == null) {
            recepcionista.setFechaRegistro(LocalDateTime.now());
        }

        if (recepcionista.getFechaContratacion() == null) {
            recepcionista.setFechaContratacion(LocalDate.now());
        }

        if (recepcionista.getEstatus() == null) {
            recepcionista.setEstatus("Activo");
        }

        // Validaciones de unicidad
        if (recepcionista.getEmail() != null && recepcionistaRepository.findByEmail(recepcionista.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (recepcionista.getTelefono() != null && recepcionistaRepository.findByTelefono(recepcionista.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado");
        }

        Recepcionista recepcionistaGuardado = recepcionistaRepository.save(recepcionista);
        System.out.println("✅ Recepcionista guardado exitosamente: " + recepcionistaGuardado.getIdRecepcionista());

        return recepcionistaGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<Recepcionista> obtenerTodosLosRecepcionistas() {
        return recepcionistaRepository.findAll();
    }

    public Recepcionista obtenerRecepcionistaPorId(String idRecepcionista) {
        Optional<Recepcionista> recepcionista = recepcionistaRepository.findById(idRecepcionista);
        return recepcionista.orElseThrow(() -> new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista));
    }

    public List<Recepcionista> obtenerRecepcionistasFiltrados(String estatus) {
        if (estatus != null) {
            return recepcionistaRepository.findByEstatus(estatus);
        } else {
            return recepcionistaRepository.findAll();
        }
    }

    @Transactional
    public Recepcionista actualizarRecepcionista(String idRecepcionista, Recepcionista recepcionistaActualizado) {
        System.out.println("✏ Actualizando recepcionista: " + idRecepcionista);

        Recepcionista recepcionistaExistente = obtenerRecepcionistaPorId(idRecepcionista);

        if (recepcionistaActualizado.getNombre() != null) {
            recepcionistaExistente.setNombre(recepcionistaActualizado.getNombre());
        }
        if (recepcionistaActualizado.getTelefono() != null) {
            recepcionistaExistente.setTelefono(recepcionistaActualizado.getTelefono());
        }
        if (recepcionistaActualizado.getEmail() != null) {
            recepcionistaExistente.setEmail(recepcionistaActualizado.getEmail());
        }
        if (recepcionistaActualizado.getFechaContratacion() != null) {
            recepcionistaExistente.setFechaContratacion(recepcionistaActualizado.getFechaContratacion());
        }
        if (recepcionistaActualizado.getEstatus() != null) {
            recepcionistaExistente.setEstatus(recepcionistaActualizado.getEstatus());
        }

        // Validaciones de unicidad
        if (recepcionistaActualizado.getEmail() != null &&
                recepcionistaRepository.existsByEmailAndNotId(recepcionistaActualizado.getEmail(), idRecepcionista)) {
            throw new RuntimeException("El email ya está registrado por otro recepcionista");
        }

        if (recepcionistaActualizado.getTelefono() != null &&
                recepcionistaRepository.existsByTelefonoAndNotId(recepcionistaActualizado.getTelefono(), idRecepcionista)) {
            throw new RuntimeException("El teléfono ya está registrado por otro recepcionista");
        }

        Recepcionista recepcionistaActualizadoDb = recepcionistaRepository.save(recepcionistaExistente);
        System.out.println("✅ Recepcionista actualizado: " + recepcionistaActualizadoDb.getIdRecepcionista());

        return recepcionistaActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public Recepcionista cambiarEstatusRecepcionista(String idRecepcionista, String nuevoEstatus) {
        Recepcionista recepcionista = obtenerRecepcionistaPorId(idRecepcionista);
        recepcionista.setEstatus(nuevoEstatus);
        return recepcionistaRepository.save(recepcionista);
    }

    public Recepcionista desactivarRecepcionista(String idRecepcionista) {
        return cambiarEstatusRecepcionista(idRecepcionista, "Inactivo");
    }

    public Recepcionista activarRecepcionista(String idRecepcionista) {
        return cambiarEstatusRecepcionista(idRecepcionista, "Activo");
    }

    public void eliminarRecepcionista(String idRecepcionista) {
        desactivarRecepcionista(idRecepcionista);
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasRecepcionista(String idRecepcionista) {
        if (!recepcionistaRepository.existsById(idRecepcionista)) {
            throw new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista);
        }

        String query = """
            SELECT 
                (SELECT COUNT(*) FROM REGISTRA r WHERE r.Id_Recepcionista = :idRecepcionista) as total_registros,
                (SELECT COUNT(*) FROM VENDE v WHERE v.Id_Recepcionista = :idRecepcionista) as total_ventas,
                (SELECT COALESCE(SUM(v.Total), 0) FROM VENDE v WHERE v.Id_Recepcionista = :idRecepcionista) as total_ingresos,
                (SELECT COUNT(*) FROM CLIENTE c WHERE c.Fecha_Registro >= CURRENT_DATE - INTERVAL '30 days') as registros_ultimo_mes
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("idRecepcionista", idRecepcionista)
                .getResultList();

        Map<String, Object> estadisticas = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            estadisticas.put("totalRegistros", result[0]);
            estadisticas.put("totalVentas", result[1]);
            estadisticas.put("totalIngresos", result[2]);
            estadisticas.put("registrosUltimoMes", result[3]);
        } else {
            estadisticas.put("totalRegistros", 0);
            estadisticas.put("totalVentas", 0);
            estadisticas.put("totalIngresos", 0.0);
            estadisticas.put("registrosUltimoMes", 0);
        }

        return estadisticas;
    }

    // ==================== CONSULTAS ADICIONALES ====================

    public boolean existeRecepcionista(String idRecepcionista) {
        return recepcionistaRepository.existsById(idRecepcionista);
    }

    public List<Recepcionista> obtenerRecepcionistasActivos() {
        return recepcionistaRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    public List<Recepcionista> buscarRecepcionistasPorNombre(String nombre) {
        return recepcionistaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Long contarRecepcionistasActivos() {
        return recepcionistaRepository.countByEstatus("Activo");
    }

    public List<Recepcionista> obtenerRecepcionistasContratadosEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
        return recepcionistaRepository.findByFechaContratacionBetween(fechaInicio, fechaFin);
    }

    // ==================== ELIMINAR RECEPCIONISTA ====================

    @Transactional
    public void eliminarRecepcionistaCompleto(String idRecepcionista) {
        Recepcionista recepcionista = obtenerRecepcionistaPorId(idRecepcionista);
        recepcionistaRepository.delete(recepcionista);
        System.out.println("✅ Recepcionista eliminado completamente: " + idRecepcionista);
    }

    // ==================== DASHBOARD ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalRecepcionistas", recepcionistaRepository.count());
        estadisticas.put("recepcionistasActivos", recepcionistaRepository.countByEstatus("Activo"));
        estadisticas.put("recepcionistasInactivos", recepcionistaRepository.countByEstatus("Inactivo"));

        // Recepcionistas contratados este mes
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        estadisticas.put("recepcionistasContratadosEsteMes",
                recepcionistaRepository.findByFechaContratacionBetween(inicioMes, finMes).size());

        return estadisticas;
    }
}