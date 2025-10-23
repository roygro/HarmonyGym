package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private RolRepository rolRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== AUTENTICACIÓN ====================

    @Transactional
    public AuthResponse autenticarUsuario(String username, String password) {
        try {
            System.out.println("🔐 Intentando autenticar usuario: " + username);

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado: " + username);
                return new AuthResponse(false, "Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar estatus del usuario
            if ("Bloqueado".equals(usuario.getEstatus())) {
                System.out.println("❌ Usuario bloqueado: " + username);
                return new AuthResponse(false, "Usuario bloqueado. Contacte al administrador.");
            }

            if ("Inactivo".equals(usuario.getEstatus())) {
                System.out.println("❌ Usuario inactivo: " + username);
                return new AuthResponse(false, "Usuario inactivo");
            }

            // Verificar contraseña (en un sistema real usarías bcrypt)
            if (!usuario.getPasswordHash().equals(password)) {
                // Incrementar intentos fallidos usando el método del repositorio
                usuarioRepository.incrementarIntentosLogin(username);

                // Obtener el usuario actualizado para verificar los intentos
                usuario = usuarioRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de incrementar intentos"));

                // Bloquear usuario después de 5 intentos fallidos
                if (usuario.getIntentosLogin() >= 5) {
                    usuarioRepository.bloquearUsuario(username, LocalDateTime.now());
                    System.out.println("❌ Usuario bloqueado por demasiados intentos fallidos: " + username);
                    return new AuthResponse(false, "Demasiados intentos fallidos. Usuario bloqueado.");
                }

                System.out.println("❌ Contraseña incorrecta para usuario: " + username);
                return new AuthResponse(false, "Credenciales incorrectas");
            }

            // Login exitoso - Actualizar último login usando el método del repositorio
            usuarioRepository.actualizarUltimoLogin(username, LocalDateTime.now());

            // Obtener información del rol y datos de la persona
            AuthResponse response = obtenerInformacionUsuarioCompleta(usuario);
            response.setSuccess(true);
            response.setMessage("Login exitoso");

            System.out.println("✅ Login exitoso para usuario: " + username);
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error durante autenticación: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(false, "Error interno del servidor");
        }
    }

    // ==================== OBTENER INFORMACIÓN COMPLETA DEL USUARIO ====================

    private AuthResponse obtenerInformacionUsuarioCompleta(Usuario usuario) {
        AuthResponse response = new AuthResponse();
        response.setIdUsuario(usuario.getIdUsuario());
        response.setUsername(usuario.getUsername());
        response.setTipoUsuario(usuario.getTipoUsuario());
        response.setIdPersona(usuario.getIdPersona());
        response.setUltimoLogin(usuario.getUltimoLogin());

        try {
            // Obtener rol del usuario
            Optional<UsuarioRol> usuarioRolOpt = usuarioRolRepository.findRolPrincipal(
                    usuario.getIdUsuario(), usuario.getTipoUsuario());

            if (usuarioRolOpt.isPresent()) {
                UsuarioRol usuarioRol = usuarioRolOpt.get();
                Optional<Rol> rolOpt = rolRepository.findById(usuarioRol.getIdRol());

                if (rolOpt.isPresent()) {
                    Rol rol = rolOpt.get();
                    response.setNombreRol(rol.getNombreRol());
                    response.setPermisos(rol.getPermisos());
                }
            }

            // Obtener nombre completo según el tipo de usuario
            String nombreCompleto = obtenerNombreCompleto(usuario.getTipoUsuario(), usuario.getIdPersona());
            response.setNombreCompleto(nombreCompleto);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener información adicional del usuario: " + e.getMessage());
        }

        return response;
    }

    // ==================== OBTENER NOMBRE COMPLETO SEGÚN TIPO DE USUARIO ====================

    private String obtenerNombreCompleto(String tipoUsuario, String idPersona) {
        try {
            String query = "";

            switch (tipoUsuario) {
                case "Administrador":
                    query = "SELECT a.nombrecom FROM administrador a WHERE a.folio_admin = :idPersona";
                    break;
                case "Recepcionista":
                    query = "SELECT r.nombre FROM recepcionista r WHERE r.id_recepcionista = :idPersona";
                    break;
                case "Instructor":
                    query = "SELECT i.nombre FROM instructor i WHERE i.folio_instructor = :idPersona";
                    break;
                case "Cliente":
                    query = "SELECT c.nombre FROM cliente c WHERE c.folio_cliente = :idPersona";
                    break;
                default:
                    return "Usuario";
            }

            List<?> results = entityManager.createNativeQuery(query)
                    .setParameter("idPersona", idPersona)
                    .getResultList();

            if (!results.isEmpty()) {
                return results.get(0).toString();
            }

        } catch (Exception e) {
            System.err.println("❌ Error al obtener nombre completo: " + e.getMessage());
        }

        return "Usuario";
    }

    // ==================== REGISTRO DE USUARIOS ====================

    @Transactional
    public AuthResponse registrarUsuario(RegisterRequest registerRequest) {
        try {
            System.out.println("🚀 Iniciando registro de usuario: " + registerRequest.getUsername());

            // Verificar si el username ya existe
            if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
                System.out.println("❌ Username ya existe: " + registerRequest.getUsername());
                return new AuthResponse(false, "El nombre de usuario ya está en uso");
            }

            // Verificar si ya existe un usuario para esta persona y tipo
            if (usuarioRepository.existsByIdPersonaAndTipoUsuario(
                    registerRequest.getIdPersona(), registerRequest.getTipoUsuario())) {
                System.out.println("❌ Ya existe un usuario para esta persona y tipo: " + registerRequest.getIdPersona());
                return new AuthResponse(false, "Ya existe un usuario para esta persona con el tipo especificado");
            }

            // Generar ID de usuario
            String idUsuario = generarIdUsuario();

            // Crear usuario
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuario);
            usuario.setUsername(registerRequest.getUsername());
            usuario.setPasswordHash(registerRequest.getPassword()); // En producción usar bcrypt
            usuario.setTipoUsuario(registerRequest.getTipoUsuario());
            usuario.setIdPersona(registerRequest.getIdPersona());
            usuario.setEstatus("Activo");
            usuario.setIntentosLogin(0);
            usuario.setFechaCreacion(LocalDateTime.now());

            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            System.out.println("✅ Usuario guardado: " + idUsuario);

            // Asignar rol por defecto según el tipo de usuario
            boolean rolAsignado = asignarRolPorDefecto(usuarioGuardado);

            if (!rolAsignado) {
                System.out.println("⚠ No se pudo asignar rol por defecto para: " + registerRequest.getTipoUsuario());
            }

            // Obtener información completa para la respuesta
            AuthResponse response = obtenerInformacionUsuarioCompleta(usuarioGuardado);
            response.setSuccess(true);
            response.setMessage("Usuario registrado exitosamente");

            System.out.println("✅ Registro completado para: " + registerRequest.getUsername());
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error durante el registro: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(false, "Error durante el registro: " + e.getMessage());
        }
    }

    // ==================== ASIGNAR ROL POR DEFECTO ====================

    private boolean asignarRolPorDefecto(Usuario usuario) {
        try {
            // Buscar rol por defecto para el tipo de usuario
            Optional<Rol> rolOpt = rolRepository.findRolPorDefecto(usuario.getTipoUsuario());

            if (rolOpt.isPresent()) {
                Rol rol = rolOpt.get();

                UsuarioRol usuarioRol = new UsuarioRol();
                usuarioRol.setIdUsuario(usuario.getIdUsuario());
                usuarioRol.setTipoUsuario(usuario.getTipoUsuario());
                usuarioRol.setIdRol(rol.getIdRol());
                usuarioRol.setEstatus("Activo");

                usuarioRolRepository.save(usuarioRol);
                System.out.println("✅ Rol asignado: " + rol.getNombreRol() + " para usuario: " + usuario.getUsername());
                return true;
            }

            System.err.println("❌ No se encontró rol por defecto para tipo: " + usuario.getTipoUsuario());
            return false;

        } catch (Exception e) {
            System.err.println("❌ Error al asignar rol por defecto: " + e.getMessage());
            return false;
        }
    }

    // ==================== GENERACIÓN DE ID DE USUARIO ====================

    private String generarIdUsuario() {
        try {
            System.out.println("🔍 Buscando último ID de usuario...");

            // Usar el método optimizado del repositorio
            String ultimoId = usuarioRepository.findUltimoIdUsuario();

            if (ultimoId == null) {
                System.out.println("✅ No hay usuarios, empezando con USR001");
                return "USR001";
            }

            // Extraer el número del último ID
            if (ultimoId.startsWith("USR")) {
                try {
                    String numeroStr = ultimoId.substring(3);
                    int numero = Integer.parseInt(numeroStr);
                    int nuevoNumero = numero + 1;
                    String nuevoId = String.format("USR%03d", nuevoNumero);

                    System.out.println("📊 Último ID encontrado: " + ultimoId);
                    System.out.println("🎯 Nuevo ID generado: " + nuevoId);

                    return nuevoId;
                } catch (NumberFormatException e) {
                    System.err.println("⚠ ID con formato inválido: " + ultimoId);
                }
            }

            // Fallback: contar usuarios
            long totalUsuarios = usuarioRepository.count();
            String idFallback = String.format("USR%03d", totalUsuarios + 1);
            System.out.println("🔄 Usando fallback: " + idFallback);
            return idFallback;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar ID: " + e.getMessage());
            e.printStackTrace();

            long totalUsuarios = usuarioRepository.count();
            String idFallback = String.format("USR%03d", totalUsuarios + 1);
            System.out.println("🔄 Usando fallback por error: " + idFallback);
            return idFallback;
        }
    }

    // ==================== CAMBIAR CONTRASEÑA ====================

    @Transactional
    public Map<String, Object> cambiarPassword(String username, String nuevaPassword) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return response;
            }

            // Usar el método del repositorio que también resetea intentos y bloqueo
            usuarioRepository.cambiarPassword(username, nuevaPassword);

            response.put("success", true);
            response.put("message", "Contraseña actualizada exitosamente");
            System.out.println("✅ Contraseña cambiada para usuario: " + username);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar contraseña: " + e.getMessage());
            System.err.println("❌ Error al cambiar contraseña: " + e.getMessage());
        }

        return response;
    }

    // ==================== VERIFICAR DISPONIBILIDAD DE USERNAME ====================

    public Map<String, Object> verificarDisponibilidadUsername(String username) {
        Map<String, Object> response = new HashMap<>();

        boolean disponible = !usuarioRepository.existsByUsername(username);
        response.put("disponible", disponible);
        response.put("message", disponible ? "Username disponible" : "Username no disponible");

        return response;
    }

    // ==================== OBTENER USUARIO POR ID PERSONA ====================

    public Optional<Usuario> obtenerUsuarioPorIdPersona(String idPersona) {
        return usuarioRepository.findByIdPersona(idPersona);
    }

    // ==================== DESBLOQUEAR USUARIO ====================

    @Transactional
    public Map<String, Object> desbloquearUsuario(String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return response;
            }

            // Para desbloquear, cambiamos la contraseña (lo cual también desbloquea)
            // o podríamos crear un método específico en el repositorio
            Usuario usuario = usuarioOpt.get();
            usuario.setEstatus("Activo");
            usuario.setFechaBloqueo(null);
            usuario.setIntentosLogin(0);
            usuarioRepository.save(usuario);

            response.put("success", true);
            response.put("message", "Usuario desbloqueado exitosamente");
            System.out.println("✅ Usuario desbloqueado: " + username);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al desbloquear usuario: " + e.getMessage());
            System.err.println("❌ Error al desbloquear usuario: " + e.getMessage());
        }

        return response;
    }

    // ==================== OBTENER USUARIO POR USERNAME ====================

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}