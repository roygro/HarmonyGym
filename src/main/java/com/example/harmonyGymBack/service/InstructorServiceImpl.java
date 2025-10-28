package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.model.Usuario;
import com.example.harmonyGymBack.model.AuthResponse;
import com.example.harmonyGymBack.model.RegisterRequest;
import com.example.harmonyGymBack.repository.InstructorRepository;
import com.example.harmonyGymBack.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InstructorServiceImpl {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private EmailService emailService;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== MÉTODOS PARA CREACIÓN DE USUARIO AUTOMÁTICO ====================

    private void crearUsuarioParaInstructor(Instructor instructor) {
        try {
            System.out.println("👤 Creando usuario automático para instructor: " + instructor.getFolioInstructor());

            // Verificar si ya existe un usuario para este instructor
            Optional<Usuario> usuarioExistente = authService.obtenerUsuarioPorIdPersona(instructor.getFolioInstructor());
            if (usuarioExistente.isPresent()) {
                System.out.println("ℹ️ Ya existe un usuario para este instructor: " + instructor.getFolioInstructor());
                return;
            }

            // Generar username único basado en el email o nombre
            String username = generarUsernameParaInstructor(instructor);

            // Generar password temporal
            String passwordTemporal = generarPasswordTemporal();

            // Crear request de registro
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(username);
            registerRequest.setPassword(passwordTemporal);
            registerRequest.setTipoUsuario("Instructor");
            registerRequest.setIdPersona(instructor.getFolioInstructor());

            // Registrar el usuario
            AuthResponse authResponse = authService.registrarUsuario(registerRequest);

            if (authResponse.isSuccess()) {
                System.out.println("✅ Usuario creado exitosamente:");
                System.out.println("   📧 Username: " + username);
                System.out.println("   🔑 Password temporal: " + passwordTemporal);
                System.out.println("   👤 ID Usuario: " + authResponse.getIdUsuario());

                // ✅ ENVIAR CREDENCIALES POR EMAIL
                if (instructor.getEmail() != null && !instructor.getEmail().trim().isEmpty()) {
                    try {
                        emailService.enviarCredencialesInstructor(
                                instructor.getEmail(),
                                instructor.getNombre(),
                                username,
                                passwordTemporal
                        );
                        System.out.println("✅ Credenciales enviadas por email a: " + instructor.getEmail());
                    } catch (Exception e) {
                        System.err.println("⚠️ No se pudieron enviar las credenciales por email: " + e.getMessage());
                        // No revertimos la creación por fallo en el email
                    }
                } else {
                    System.out.println("⚠️ Instructor no tiene email, no se enviaron credenciales");
                }

            } else {
                throw new RuntimeException("Error al crear usuario: " + authResponse.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario automático: " + e.getMessage());
            throw new RuntimeException("No se pudo crear el usuario automáticamente: " + e.getMessage());
        }
    }

    private String generarUsernameParaInstructor(Instructor instructor) {
        String usernameBase = "";

        // Prioridad 1: Usar el email como base
        if (instructor.getEmail() != null && !instructor.getEmail().trim().isEmpty()) {
            usernameBase = instructor.getEmail().split("@")[0];
        }
        // Prioridad 2: Usar el nombre + apellido
        else if (instructor.getNombre() != null && !instructor.getNombre().trim().isEmpty()) {
            String[] nombrePartes = instructor.getNombre().split("\\s+");
            if (nombrePartes.length >= 2) {
                // Usar primera letra del nombre + apellido completo
                usernameBase = nombrePartes[0].toLowerCase().charAt(0) +
                        nombrePartes[nombrePartes.length - 1].toLowerCase();
            } else {
                // Si solo tiene un nombre, usarlo completo
                usernameBase = instructor.getNombre().toLowerCase();
            }
            // Limpiar caracteres especiales
            usernameBase = usernameBase.replaceAll("[^a-zA-Z0-9]", "");
        }
        // Prioridad 3: Usar el folio del instructor
        else {
            usernameBase = "instructor" + instructor.getFolioInstructor().toLowerCase();
        }

        // Verificar disponibilidad y generar username único
        return generarUsernameUnico(usernameBase);
    }

    private String generarUsernameUnico(String base) {
        String username = base;
        int counter = 1;

        // Verificar disponibilidad usando el AuthService
        Map<String, Object> disponibilidad = authService.verificarDisponibilidadUsername(username);
        boolean disponible = (Boolean) disponibilidad.get("disponible");

        while (!disponible) {
            username = base + counter;
            disponibilidad = authService.verificarDisponibilidadUsername(username);
            disponible = (Boolean) disponibilidad.get("disponible");
            counter++;

            // Prevenir loop infinito
            if (counter > 100) {
                throw new RuntimeException("No se pudo generar un username único después de 100 intentos");
            }
        }

        return username;
    }

    private String generarPasswordTemporal() {
        // Generar una contraseña temporal de 8 caracteres
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        // Asegurar que tenga al menos una mayúscula, una minúscula y un número
        password.append(caracteres.charAt((int) (Math.random() * 26))); // Mayúscula
        password.append(caracteres.charAt(26 + (int) (Math.random() * 26))); // Minúscula
        password.append(caracteres.charAt(52 + (int) (Math.random() * 10))); // Número

        // Completar los 8 caracteres
        for (int i = 3; i < 8; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }

        // Mezclar los caracteres
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private void sincronizarEstatusUsuario(String folioInstructor, String estatusInstructor) {
        try {
            Optional<Usuario> usuarioOpt = authService.obtenerUsuarioPorIdPersona(folioInstructor);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String estatusUsuario = "Activo".equals(estatusInstructor) ? "Activo" : "Inactivo";

                if (!estatusUsuario.equals(usuario.getEstatus())) {
                    usuario.setEstatus(estatusUsuario);
                    // Si se activa el instructor, resetear intentos de login
                    if ("Activo".equals(estatusUsuario)) {
                        usuario.setIntentosLogin(0);
                        usuario.setFechaBloqueo(null);
                    }

                    // Guardar cambios en el usuario
                    usuarioRepository.save(usuario);

                    System.out.println("✅ Estatus de usuario actualizado: " + usuario.getUsername() + " -> " + estatusUsuario);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al sincronizar estatus de usuario: " + e.getMessage());
            throw new RuntimeException("Error al sincronizar estatus de usuario");
        }
    }

    // ==================== GENERACIÓN AUTOMÁTICA DE FOLIO ====================

    private String generarFolioInstructor() {
        try {
            System.out.println("🔍 Buscando último folio de instructor...");

            List<Instructor> todosInstructores = instructorRepository.findAll();

            if (todosInstructores.isEmpty()) {
                System.out.println("✅ No hay instructores, empezando con INS001");
                return "INS001";
            }

            String ultimoFolio = null;
            int maxNumero = 0;

            for (Instructor instructor : todosInstructores) {
                String folio = instructor.getFolioInstructor();
                if (folio != null && folio.startsWith("INS")) {
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
                System.out.println("✅ No se encontraron folios válidos, empezando con INS001");
                return "INS001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoFolio = String.format("INS%03d", nuevoNumero);

            System.out.println("📊 Último folio encontrado: " + ultimoFolio);
            System.out.println("🎯 Nuevo folio generado: " + nuevoFolio);

            return nuevoFolio;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar folio: " + e.getMessage());
            e.printStackTrace();

            long totalInstructores = instructorRepository.count();
            String folioFallback = String.format("INS%03d", totalInstructores + 1);
            System.out.println("🔄 Usando fallback: " + folioFallback);
            return folioFallback;
        }
    }

    // ==================== CREAR NUEVO INSTRUCTOR ====================

    @Transactional
    public Instructor crearInstructorConCredenciales(Instructor instructor, String email) {
        System.out.println("🚀 Iniciando creación de instructor con credenciales...");

        // ✅ VALIDAR QUE EL EMAIL ES OBLIGATORIO
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio para crear las credenciales de acceso");
        }

        // Asignar el email al instructor
        instructor.setEmail(email);

        // Generar folio automáticamente
        String folioGenerado = generarFolioInstructor();
        instructor.setFolioInstructor(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        // Validaciones básicas
        if (instructor.getFechaContratacion() == null) {
            instructor.setFechaContratacion(LocalDate.now());
        }

        if (instructor.getEstatus() == null) {
            instructor.setEstatus("Activo");
        }

        // Validaciones de unicidad
        if (instructorRepository.findByEmail(instructor.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado para otro instructor");
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        Instructor instructorGuardado = instructorRepository.save(instructor);
        System.out.println("✅ Instructor creado exitosamente: " + folioGenerado);

        // ✅ Crear usuario automáticamente para el instructor
        try {
            crearUsuarioParaInstructor(instructorGuardado);
            System.out.println("✅ Usuario creado automáticamente para el instructor: " + folioGenerado);
        } catch (Exception e) {
            System.err.println("⚠ Advertencia: No se pudo crear el usuario para el instructor: " + e.getMessage());
            // No lanzamos excepción para no revertir la creación del instructor
        }

        return instructorGuardado;
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Instructor crearInstructor(Instructor instructor) {
        System.out.println("🚀 Iniciando creación de instructor...");

        // ✅ VALIDAR QUE EL EMAIL ES OBLIGATORIO
        if (instructor.getEmail() == null || instructor.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio para crear las credenciales de acceso");
        }

        String folioGenerado = generarFolioInstructor();
        instructor.setFolioInstructor(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        if (instructor.getFechaContratacion() == null) {
            instructor.setFechaContratacion(LocalDate.now());
        }

        if (instructor.getEstatus() == null) {
            instructor.setEstatus("Activo");
        }

        // Validaciones de unicidad
        if (instructorRepository.findByEmail(instructor.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado para otro instructor");
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        Instructor instructorGuardado = instructorRepository.save(instructor);
        System.out.println("✅ Instructor guardado exitosamente: " + instructorGuardado.getFolioInstructor());

        // ✅ Crear usuario automáticamente
        try {
            crearUsuarioParaInstructor(instructorGuardado);
            System.out.println("✅ Usuario creado automáticamente para el instructor: " + folioGenerado);
        } catch (Exception e) {
            System.err.println("⚠ Advertencia: No se pudo crear el usuario para el instructor: " + e.getMessage());
        }

        return instructorGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<Instructor> obtenerTodosLosInstructores() {
        return instructorRepository.findAll();
    }

    public Instructor obtenerInstructorPorId(String folioInstructor) {
        Optional<Instructor> instructor = instructorRepository.findByFolioInstructor(folioInstructor);
        return instructor.orElseThrow(() -> new RuntimeException("Instructor no encontrado con folio: " + folioInstructor));
    }

    public List<Instructor> obtenerInstructoresFiltrados(String estatus, String especialidad) {
        if (estatus != null && especialidad != null) {
            return instructorRepository.findByEstatusAndEspecialidadContainingIgnoreCase(estatus, especialidad);
        } else if (estatus != null) {
            return instructorRepository.findByEstatus(estatus);
        } else if (especialidad != null) {
            return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
        } else {
            return instructorRepository.findAll();
        }
    }

    @Transactional
    public Instructor actualizarInstructor(String folioInstructor, Instructor instructorActualizado) {
        System.out.println("✏ Actualizando instructor: " + folioInstructor);

        Instructor instructorExistente = obtenerInstructorPorId(folioInstructor);

        if (instructorActualizado.getNombre() != null) {
            instructorExistente.setNombre(instructorActualizado.getNombre());
        }
        if (instructorActualizado.getApp() != null) {
            instructorExistente.setApp(instructorActualizado.getApp());
        }
        if (instructorActualizado.getApm() != null) {
            instructorExistente.setApm(instructorActualizado.getApm());
        }
        if (instructorActualizado.getEmail() != null) {
            instructorExistente.setEmail(instructorActualizado.getEmail());
        }
        if (instructorActualizado.getHoraEntrada() != null) {
            instructorExistente.setHoraEntrada(instructorActualizado.getHoraEntrada());
        }
        if (instructorActualizado.getHoraSalida() != null) {
            instructorExistente.setHoraSalida(instructorActualizado.getHoraSalida());
        }
        if (instructorActualizado.getEspecialidad() != null) {
            instructorExistente.setEspecialidad(instructorActualizado.getEspecialidad());
        }
        if (instructorActualizado.getFechaContratacion() != null) {
            instructorExistente.setFechaContratacion(instructorActualizado.getFechaContratacion());
        }
        if (instructorActualizado.getEstatus() != null) {
            instructorExistente.setEstatus(instructorActualizado.getEstatus());
        }

        // Validaciones de unicidad
        if (instructorActualizado.getEmail() != null &&
                instructorRepository.existsByEmailAndFolioInstructorNot(instructorActualizado.getEmail(), folioInstructor)) {
            throw new RuntimeException("El email ya está registrado por otro instructor");
        }

        // Validar horarios
        if (instructorExistente.getHoraEntrada() != null && instructorExistente.getHoraSalida() != null) {
            if (instructorExistente.getHoraEntrada().isAfter(instructorExistente.getHoraSalida()) ||
                    instructorExistente.getHoraEntrada().equals(instructorExistente.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        Instructor instructorActualizadoDb = instructorRepository.save(instructorExistente);
        System.out.println("✅ Instructor actualizado: " + instructorActualizadoDb.getFolioInstructor());

        // ✅ Sincronizar estatus con el usuario si cambió
        if (instructorActualizado.getEstatus() != null) {
            try {
                sincronizarEstatusUsuario(folioInstructor, instructorActualizado.getEstatus());
            } catch (Exception e) {
                System.err.println("⚠ Advertencia: No se pudo sincronizar el estatus del usuario: " + e.getMessage());
            }
        }

        return instructorActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public Instructor cambiarEstatusInstructor(String folioInstructor, String nuevoEstatus) {
        Instructor instructor = obtenerInstructorPorId(folioInstructor);
        instructor.setEstatus(nuevoEstatus);

        Instructor instructorActualizado = instructorRepository.save(instructor);

        // ✅ Sincronizar estatus con el usuario
        try {
            sincronizarEstatusUsuario(folioInstructor, nuevoEstatus);
        } catch (Exception e) {
            System.err.println("⚠ Advertencia: No se pudo sincronizar el estatus del usuario: " + e.getMessage());
        }

        return instructorActualizado;
    }

    public Instructor desactivarInstructor(String folioInstructor) {
        return cambiarEstatusInstructor(folioInstructor, "Inactivo");
    }

    public Instructor activarInstructor(String folioInstructor) {
        return cambiarEstatusInstructor(folioInstructor, "Activo");
    }

    public void eliminarInstructor(String folioInstructor) {
        // Primero desactivar el instructor
        Instructor instructorDesactivado = cambiarEstatusInstructor(folioInstructor, "Inactivo");

        // Luego desactivar el usuario asociado
        try {
            sincronizarEstatusUsuario(folioInstructor, "Inactivo");
            System.out.println("✅ Instructor y usuario desactivados: " + folioInstructor);
        } catch (Exception e) {
            System.err.println("⚠ Instructor desactivado pero no se pudo desactivar el usuario: " + e.getMessage());
        }
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasInstructor(String folioInstructor) {
        if (!instructorRepository.existsByFolioInstructor(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }

        String query = """
            SELECT 
                COUNT(DISTINCT c.Id_Actividad) as total_actividades,
                COUNT(DISTINCT a.Folio_Rutina) as total_rutinas_asignadas,
                COUNT(DISTINCT ra.Folio_Cliente) as total_clientes_actividades,
                COUNT(DISTINCT rr.Folio_Cliente) as total_clientes_rutinas,
                COALESCE(AVG(ra.Calificacion), 0) as promedio_calificacion_actividades,
                COALESCE(AVG(rr.Calificacion), 0) as promedio_calificacion_rutinas
            FROM INSTRUCTOR i
            LEFT JOIN CREA c ON i.Folio_Instructor = c.Folio_Instructor
            LEFT JOIN ASIGNA a ON i.Folio_Instructor = a.Folio_Instructor
            LEFT JOIN ACTIVIDAD act ON c.Id_Actividad = act.Id_Actividad
            LEFT JOIN REALIZA_ACTIVIDAD ra ON act.Id_Actividad = ra.Id_Actividad
            LEFT JOIN REALIZA_RUTINA rr ON a.Folio_Rutina = rr.Folio_Rutina
            WHERE i.Folio_Instructor = :folioInstructor
            GROUP BY i.Folio_Instructor
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("folioInstructor", folioInstructor)
                .getResultList();

        Map<String, Object> estadisticas = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            estadisticas.put("totalActividades", result[0]);
            estadisticas.put("totalRutinasAsignadas", result[1]);
            estadisticas.put("totalClientesActividades", result[2]);
            estadisticas.put("totalClientesRutinas", result[3]);
            estadisticas.put("promedioCalificacionActividades", result[4]);
            estadisticas.put("promedioCalificacionRutinas", result[5]);
        } else {
            estadisticas.put("totalActividades", 0);
            estadisticas.put("totalRutinasAsignadas", 0);
            estadisticas.put("totalClientesActividades", 0);
            estadisticas.put("totalClientesRutinas", 0);
            estadisticas.put("promedioCalificacionActividades", 0.0);
            estadisticas.put("promedioCalificacionRutinas", 0.0);
        }

        return estadisticas;
    }

    // ==================== CONSULTAS ADICIONALES ====================

    public boolean existeInstructor(String folioInstructor) {
        return instructorRepository.existsByFolioInstructor(folioInstructor);
    }

    public boolean existeInstructorPorEmail(String email) {
        return instructorRepository.existsByEmail(email);
    }

    public List<Instructor> obtenerInstructoresActivos() {
        return instructorRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    public List<Instructor> buscarInstructoresPorNombre(String nombre) {
        return instructorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Instructor> buscarInstructoresPorEmail(String email) {
        return instructorRepository.findByEmailContainingIgnoreCase(email);
    }

    public Long contarInstructoresActivos() {
        return instructorRepository.findByEstatus("Activo").stream().count();
    }

    public List<Instructor> obtenerInstructoresPorEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    // ==================== ELIMINAR INSTRUCTOR COMPLETO ====================

    @Transactional
    public void eliminarInstructorCompleto(String folioInstructor) {
        Instructor instructor = obtenerInstructorPorId(folioInstructor);

        // Eliminar de la base de datos
        instructorRepository.delete(instructor);
        System.out.println("✅ Instructor eliminado completamente: " + folioInstructor);
    }

    // ==================== MÉTODO PARA CREAR CON EMAIL (compatibilidad con frontend) ====================

    @Transactional
    public Instructor crearInstructorConEmail(Instructor instructor, String email, String username) {
        System.out.println("🚀 Iniciando creación de instructor con email y credenciales...");

        // ✅ VALIDAR QUE EL EMAIL ES OBLIGATORIO
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio para crear las credenciales de acceso");
        }

        // Asignar el email al instructor
        instructor.setEmail(email);

        // Generar folio automáticamente si no viene
        if (instructor.getFolioInstructor() == null || instructor.getFolioInstructor().trim().isEmpty()) {
            String folioGenerado = generarFolioInstructor();
            instructor.setFolioInstructor(folioGenerado);
            System.out.println("📝 Folio asignado: " + folioGenerado);
        }

        // Validaciones básicas
        if (instructor.getFechaContratacion() == null) {
            instructor.setFechaContratacion(LocalDate.now());
        }

        if (instructor.getEstatus() == null) {
            instructor.setEstatus("Activo");
        }

        // Validaciones de unicidad
        if (instructorRepository.findByEmail(instructor.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado para otro instructor");
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Guardar instructor primero
        Instructor instructorGuardado = instructorRepository.save(instructor);
        System.out.println("✅ Instructor guardado exitosamente: " + instructorGuardado.getFolioInstructor());

        // ✅ CREAR USUARIO AUTOMÁTICAMENTE (ESTO ES LO QUE FALTA)
        try {
            crearUsuarioParaInstructor(instructorGuardado);
            System.out.println("✅ Usuario creado automáticamente para el instructor: " + instructorGuardado.getFolioInstructor());
        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario para instructor: " + e.getMessage());
            // Puedes decidir si revertir la creación del instructor o solo loggear el error
            throw new RuntimeException("No se pudo crear el usuario automáticamente: " + e.getMessage());
        }

        return instructorGuardado;
    }

    // ==================== MÉTODOS DE PARÁMETROS (para compatibilidad) ====================

    @Transactional
    public Instructor crearInstructor(String nombre, String app, String apm, String email,
                                      String horaEntrada, String horaSalida,
                                      String especialidad, String fechaContratacion,
                                      String estatus) {
        System.out.println("🚀 Iniciando creación de instructor...");

        // Generar folio
        String folioGenerado = generarFolioInstructor();

        // Validar y limpiar email
        String emailLimpio = limpiarEmail(email);

        // Verificar si el email ya existe (solo si no es nulo)
        if (emailLimpio != null && instructorRepository.existsByEmail(emailLimpio)) {
            throw new RuntimeException("El email " + emailLimpio + " ya está registrado para otro instructor");
        }

        // Crear objeto Instructor
        Instructor instructor = new Instructor();
        instructor.setFolioInstructor(folioGenerado);
        instructor.setNombre(nombre);
        instructor.setApp(app);
        instructor.setApm(apm);
        instructor.setEmail(emailLimpio); // Usar email limpio
        instructor.setEspecialidad(especialidad);
        instructor.setEstatus(estatus != null ? estatus : "Activo");

        // Convertir y validar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructor.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructor.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructor.setFechaContratacion(LocalDate.parse(fechaContratacion));
        } else {
            instructor.setFechaContratacion(LocalDate.now());
        }

        // Usar el método principal que incluye creación de usuario
        return crearInstructorConCredenciales(instructor, emailLimpio);
    }

    @Transactional
    public Instructor actualizarInstructor(String folioInstructor, String nombre, String app, String apm, String email,
                                           String horaEntrada, String horaSalida, String especialidad,
                                           String fechaContratacion, String estatus) {
        System.out.println("✏️ Actualizando instructor: " + folioInstructor);

        Instructor instructorExistente = obtenerInstructorPorId(folioInstructor);

        // Verificar si el email ya existe para otro instructor
        if (email != null && !email.trim().isEmpty()) {
            Optional<Instructor> instructorConEmail = instructorRepository.findByEmail(email);
            if (instructorConEmail.isPresent() &&
                    !instructorConEmail.get().getFolioInstructor().equals(folioInstructor)) {
                throw new RuntimeException("El email " + email + " ya está registrado para otro instructor");
            }
        }

        // Actualizar campos básicos
        if (nombre != null) instructorExistente.setNombre(nombre);
        if (app != null) instructorExistente.setApp(app);
        if (apm != null) instructorExistente.setApm(apm);
        if (email != null) instructorExistente.setEmail(email);
        if (especialidad != null) instructorExistente.setEspecialidad(especialidad);
        if (estatus != null) instructorExistente.setEstatus(estatus);

        // Actualizar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructorExistente.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructorExistente.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructorExistente.getHoraEntrada() != null && instructorExistente.getHoraSalida() != null) {
            if (instructorExistente.getHoraEntrada().isAfter(instructorExistente.getHoraSalida()) ||
                    instructorExistente.getHoraEntrada().equals(instructorExistente.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Actualizar fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructorExistente.setFechaContratacion(LocalDate.parse(fechaContratacion));
        }

        Instructor instructorActualizado = instructorRepository.save(instructorExistente);
        System.out.println("✅ Instructor actualizado: " + folioInstructor);

        return instructorActualizado;
    }

    // Método para limpiar email
    private String limpiarEmail(String email) {
        if (email == null || email.trim().isEmpty() || email.trim().equalsIgnoreCase("null")) {
            return null;
        }
        return email.trim();
    }
}