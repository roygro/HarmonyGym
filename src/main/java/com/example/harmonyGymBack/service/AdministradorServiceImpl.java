package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Administrador;
import com.example.harmonyGymBack.repository.AdministradorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdministradorServiceImpl {

    @Autowired
    private AdministradorRepository administradorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== GENERACIÓN AUTOMÁTICA DE FOLIO ====================

    private String generarFolioAdmin() {
        try {
            System.out.println("🔍 Buscando último folio de administrador...");

            List<Administrador> todosAdministradores = administradorRepository.findAll();

            if (todosAdministradores.isEmpty()) {
                System.out.println("✅ No hay administradores, empezando con ADM001");
                return "ADM001";
            }

            String ultimoFolio = null;
            int maxNumero = 0;

            for (Administrador administrador : todosAdministradores) {
                String folio = administrador.getFolioAdmin();
                if (folio != null && folio.startsWith("ADM")) {
                    try {
                        String numeroStr = folio.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                            ultimoFolio = folio;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠ Folio con formato inválido: " + folio);
                    }
                }
            }

            if (ultimoFolio == null) {
                System.out.println("✅ No se encontraron folios válidos, empezando con ADM001");
                return "ADM001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoFolio = String.format("ADM%03d", nuevoNumero);

            System.out.println("📊 Último folio encontrado: " + ultimoFolio);
            System.out.println("🎯 Nuevo folio generado: " + nuevoFolio);

            return nuevoFolio;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar folio: " + e.getMessage());
            e.printStackTrace();

            long totalAdministradores = administradorRepository.count();
            String folioFallback = String.format("ADM%03d", totalAdministradores + 1);
            System.out.println("🔄 Usando fallback: " + folioFallback);
            return folioFallback;
        }
    }

    // ==================== CREAR NUEVO ADMINISTRADOR ====================

    @Transactional
    public Administrador crearAdministrador(String nombreCom, String app, String apm) {
        System.out.println("🚀 Iniciando creación de administrador...");

        String folioGenerado = generarFolioAdmin();

        Administrador administrador = new Administrador();
        administrador.setFolioAdmin(folioGenerado);
        administrador.setNombreCom(nombreCom);
        administrador.setApp(app);
        administrador.setApm(apm);
        administrador.setFechaRegistro(LocalDateTime.now());

        // Validar que no exista un administrador con el mismo nombre completo
        if (administradorRepository.existsByNombreCompleto(nombreCom, app, apm)) {
            throw new RuntimeException("Ya existe un administrador con el mismo nombre completo");
        }

        Administrador administradorGuardado = administradorRepository.save(administrador);
        System.out.println("✅ Administrador creado exitosamente: " + folioGenerado);

        return administradorGuardado;
    }

    // ==================== ACTUALIZAR ADMINISTRADOR ====================

    @Transactional
    public Administrador actualizarAdministrador(String folioAdmin, String nombreCom, String app, String apm) {
        System.out.println("✏ Actualizando administrador: " + folioAdmin);

        Administrador administradorExistente = obtenerAdministradorPorId(folioAdmin);

        if (nombreCom != null) administradorExistente.setNombreCom(nombreCom);
        if (app != null) administradorExistente.setApp(app);
        if (apm != null) administradorExistente.setApm(apm);

        // Validar que no exista otro administrador con el mismo nombre completo
        if (nombreCom != null && app != null && apm != null &&
                administradorRepository.existsByNombreCompletoAndNotId(nombreCom, app, apm, folioAdmin)) {
            throw new RuntimeException("Ya existe otro administrador con el mismo nombre completo");
        }

        Administrador administradorActualizado = administradorRepository.save(administradorExistente);
        System.out.println("✅ Administrador actualizado: " + folioAdmin);

        return administradorActualizado;
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Administrador crearAdministrador(Administrador administrador) {
        System.out.println("🚀 Iniciando creación de administrador...");

        String folioGenerado = generarFolioAdmin();
        administrador.setFolioAdmin(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        if (administrador.getFechaRegistro() == null) {
            administrador.setFechaRegistro(LocalDateTime.now());
        }

        // Validar que no exista un administrador con el mismo nombre completo
        if (administradorRepository.existsByNombreCompleto(
                administrador.getNombreCom(), administrador.getApp(), administrador.getApm())) {
            throw new RuntimeException("Ya existe un administrador con el mismo nombre completo");
        }

        Administrador administradorGuardado = administradorRepository.save(administrador);
        System.out.println("✅ Administrador guardado exitosamente: " + administradorGuardado.getFolioAdmin());

        return administradorGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<Administrador> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll();
    }

    public Administrador obtenerAdministradorPorId(String folioAdmin) {
        Optional<Administrador> administrador = administradorRepository.findById(folioAdmin);
        return administrador.orElseThrow(() -> new RuntimeException("Administrador no encontrado con folio: " + folioAdmin));
    }

    public List<Administrador> obtenerAdministradoresFiltrados(String nombreCom, String app, String apm) {
        if (nombreCom != null && app != null && apm != null) {
            return administradorRepository.findByNombreComContainingIgnoreCaseAndAppContainingIgnoreCaseAndApmContainingIgnoreCase(
                    nombreCom, app, apm);
        } else if (nombreCom != null) {
            return administradorRepository.findByNombreComContainingIgnoreCase(nombreCom);
        } else if (app != null) {
            return administradorRepository.findByAppContainingIgnoreCase(app);
        } else if (apm != null) {
            return administradorRepository.findByApmContainingIgnoreCase(apm);
        } else {
            return administradorRepository.findAll();
        }
    }

    @Transactional
    public Administrador actualizarAdministrador(String folioAdmin, Administrador administradorActualizado) {
        System.out.println("✏ Actualizando administrador: " + folioAdmin);

        Administrador administradorExistente = obtenerAdministradorPorId(folioAdmin);

        if (administradorActualizado.getNombreCom() != null) {
            administradorExistente.setNombreCom(administradorActualizado.getNombreCom());
        }
        if (administradorActualizado.getApp() != null) {
            administradorExistente.setApp(administradorActualizado.getApp());
        }
        if (administradorActualizado.getApm() != null) {
            administradorExistente.setApm(administradorActualizado.getApm());
        }
        if (administradorActualizado.getFechaRegistro() != null) {
            administradorExistente.setFechaRegistro(administradorActualizado.getFechaRegistro());
        }

        // Validar que no exista otro administrador con el mismo nombre completo
        if (administradorActualizado.getNombreCom() != null &&
                administradorActualizado.getApp() != null &&
                administradorActualizado.getApm() != null &&
                administradorRepository.existsByNombreCompletoAndNotId(
                        administradorActualizado.getNombreCom(),
                        administradorActualizado.getApp(),
                        administradorActualizado.getApm(),
                        folioAdmin)) {
            throw new RuntimeException("Ya existe otro administrador con el mismo nombre completo");
        }

        Administrador administradorActualizadoDb = administradorRepository.save(administradorExistente);
        System.out.println("✅ Administrador actualizado: " + administradorActualizadoDb.getFolioAdmin());

        return administradorActualizadoDb;
    }

    // ==================== ELIMINAR ADMINISTRADOR ====================

    @Transactional
    public void eliminarAdministrador(String folioAdmin) {
        Administrador administrador = obtenerAdministradorPorId(folioAdmin);
        administradorRepository.delete(administrador);
        System.out.println("✅ Administrador eliminado: " + folioAdmin);
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasAdministrador(String folioAdmin) {
        if (!administradorRepository.existsById(folioAdmin)) {
            throw new RuntimeException("Administrador no encontrado con folio: " + folioAdmin);
        }

        String query = """
            SELECT 
                (SELECT COUNT(*) FROM GESTIONA g WHERE g.Folio_Admin = :folioAdmin) as total_gestiones,
                (SELECT COUNT(*) FROM MEMBRESIA m WHERE m.Estatus = 'Activa') as total_membresias_activas,
                (SELECT COUNT(*) FROM CLIENTE c WHERE c.Estatus = 'Activo') as total_clientes_activos,
                (SELECT COUNT(*) FROM INSTRUCTOR i WHERE i.Estatus = 'Activo') as total_instructores_activos,
                (SELECT COUNT(*) FROM RECEPCIONISTA r WHERE r.Estatus = 'Activo') as total_recepcionistas_activos,
                (SELECT COALESCE(SUM(v.Total), 0) FROM VENDE v WHERE DATE(v.Fecha_Venta) = CURRENT_DATE) as ingresos_hoy
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("folioAdmin", folioAdmin)
                .getResultList();

        Map<String, Object> estadisticas = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            estadisticas.put("totalGestiones", result[0]);
            estadisticas.put("totalMembresiasActivas", result[1]);
            estadisticas.put("totalClientesActivos", result[2]);
            estadisticas.put("totalInstructoresActivos", result[3]);
            estadisticas.put("totalRecepcionistasActivos", result[4]);
            estadisticas.put("ingresosHoy", result[5]);
        } else {
            estadisticas.put("totalGestiones", 0);
            estadisticas.put("totalMembresiasActivas", 0);
            estadisticas.put("totalClientesActivos", 0);
            estadisticas.put("totalInstructoresActivos", 0);
            estadisticas.put("totalRecepcionistasActivos", 0);
            estadisticas.put("ingresosHoy", 0.0);
        }

        return estadisticas;
    }

    // ==================== CONSULTAS ADICIONALES ====================

    public boolean existeAdministrador(String folioAdmin) {
        return administradorRepository.existsById(folioAdmin);
    }

    public List<Administrador> buscarAdministradoresPorNombre(String nombreCom) {
        return administradorRepository.findByNombreComContainingIgnoreCase(nombreCom);
    }

    public List<Administrador> buscarAdministradoresPorApp(String app) {
        return administradorRepository.findByAppContainingIgnoreCase(app);
    }

    public List<Administrador> buscarAdministradoresPorApm(String apm) {
        return administradorRepository.findByApmContainingIgnoreCase(apm);
    }

    public Long contarAdministradores() {
        return administradorRepository.count();
    }

    public List<Administrador> obtenerAdministradoresRecientes(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        return administradorRepository.findAdministradoresRecientes(fechaLimite);
    }

    public List<Administrador> obtenerAdministradoresPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return administradorRepository.findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    // ==================== DASHBOARD ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalAdministradores", administradorRepository.count());
        estadisticas.put("administradoresRecientes",
                administradorRepository.findAdministradoresRecientes(LocalDateTime.now().minusDays(30)).size());

        // Estadísticas generales del sistema
        String querySistema = """
            SELECT 
                (SELECT COUNT(*) FROM CLIENTE WHERE Estatus = 'Activo') as clientes_activos,
                (SELECT COUNT(*) FROM INSTRUCTOR WHERE Estatus = 'Activo') as instructores_activos,
                (SELECT COUNT(*) FROM RECEPCIONISTA WHERE Estatus = 'Activo') as recepcionistas_activos,
                (SELECT COUNT(*) FROM MEMBRESIA WHERE Estatus = 'Activa') as membresias_activas,
                (SELECT COALESCE(SUM(Total), 0) FROM VENDE WHERE DATE(Fecha_Venta) = CURRENT_DATE) as ingresos_hoy,
                (SELECT COUNT(*) FROM ACTIVIDAD WHERE Estatus = 'Activa') as actividades_activas
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> resultsSistema = entityManager.createNativeQuery(querySistema).getResultList();

        if (!resultsSistema.isEmpty()) {
            Object[] result = resultsSistema.get(0);
            estadisticas.put("clientesActivos", result[0]);
            estadisticas.put("instructoresActivos", result[1]);
            estadisticas.put("recepcionistasActivos", result[2]);
            estadisticas.put("membresiasActivas", result[3]);
            estadisticas.put("ingresosHoy", result[4]);
            estadisticas.put("actividadesActivas", result[5]);
        }

        return estadisticas;
    }
}