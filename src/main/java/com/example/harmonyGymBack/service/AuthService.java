package com.example.harmonyGymBack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.EstatusUsuario;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.repository.*;
import com.example.harmonyGymBack.security.JwtTokenProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioRolRepository usuarioRolRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== AUTENTICACIÓN ====================

    @Transactional
    public AuthResponse autenticarUsuario(String username, String password) {
        try {
            log.info("🔐 Intentando autenticar usuario: {}", username);

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isEmpty()) {
                log.info("❌ Usuario no encontrado: {}", username);
                return new AuthResponse(false, "Credenciales incorrectas");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar estatus del usuario
            if (EstatusUsuario.Bloqueado.equals(usuario.getEstatus()) || "Bloqueado".equalsIgnoreCase(String.valueOf(usuario.getEstatus()))) {
                log.info("❌ Usuario bloqueado: {}", username);
                return new AuthResponse(false, "Usuario bloqueado. Contacte al administrador.");
            }

            if (EstatusUsuario.Inactivo.equals(usuario.getEstatus())) {
                log.info("❌ Usuario inactivo: {}", username);
                return new AuthResponse(false, "Usuario inactivo");
            }

            // Verificación y migración transparente de contraseña BCrypt
            String storedPassword = usuario.getPasswordHash();
            boolean passwordCorrecta = false;

            if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"))) {
                // Ya está hasheada con BCrypt
                passwordCorrecta = passwordEncoder.matches(password, storedPassword);
            } else {
                // Contraseña legacy en texto plano: comparar directamente y migrar si coincide
                if (storedPassword != null && storedPassword.equals(password)) {
                    passwordCorrecta = true;
                    String hashBcrypt = passwordEncoder.encode(password);
                    usuario.setPasswordHash(hashBcrypt);
                    usuarioRepository.save(usuario);
                    log.info("🔐 Contraseña legacy de {} migrada exitosamente a BCrypt", username);
                }
            }

            if (!passwordCorrecta) {
                // Incrementar intentos fallidos
                int intentos = (usuario.getIntentosLogin() != null ? usuario.getIntentosLogin() : 0) + 1;
                usuario.setIntentosLogin(intentos);

                if (intentos >= 5) {
                    usuario.setEstatus(EstatusUsuario.Bloqueado);
                    usuario.setFechaBloqueo(LocalDateTime.now());
                    usuarioRepository.save(usuario);
                    log.warn("❌ Usuario {} bloqueado por alcanzar 5 intentos fallidos", username);
                    return new AuthResponse(false, "Demasiados intentos fallidos. Usuario bloqueado.");
                }

                usuarioRepository.save(usuario);
                log.info("❌ Contraseña incorrecta para usuario: {} (intento {}/5)", username, intentos);
                return new AuthResponse(false, "Credenciales incorrectas");
            }

            // Login exitoso: resetear intentos y actualizar último login
            usuario.setIntentosLogin(0);
            usuario.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(usuario);

            // Obtener información del rol y datos de la persona
            AuthResponse response = obtenerInformacionUsuarioCompleta(usuario);
            response.setSuccess(true);
            response.setMessage("Login exitoso");
            response.setDebeCambiarPassword(usuario.getDebeCambiarPassword());

            // Generar token JWT
            String token = jwtTokenProvider.generarToken(
                    usuario.getUsername(),
                    usuario.getIdUsuario(),
                    usuario.getTipoUsuario(),
                    response.getNombreRol(),
                    usuario.getIdPersona()
            );
            response.setToken(token);

            log.info("✅ Login exitoso y token JWT emitido para usuario: {}", username);
            return response;

        } catch (Exception e) {
            log.error("❌ Error durante autenticación: {}", e.getMessage(), e);
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
        response.setDebeCambiarPassword(usuario.getDebeCambiarPassword());

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
            } else {
                response.setNombreRol(usuario.getTipoUsuario());
            }

            // Obtener nombre completo según el tipo de usuario
            String nombreCompleto = obtenerNombreCompleto(usuario.getTipoUsuario(), usuario.getIdPersona());
            response.setNombreCompleto(nombreCompleto);

        } catch (Exception e) {
            log.error("❌ Error al obtener información adicional del usuario: {}", e.getMessage());
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
            log.error("❌ Error al obtener nombre completo: {}", e.getMessage());
        }

        return "Usuario";
    }

    // ==================== REGISTRO DE USUARIOS ====================

    @Transactional
    public AuthResponse registrarUsuario(RegisterRequest registerRequest) {
        try {
            log.info("🚀 Iniciando registro de usuario: {}", registerRequest.getUsername());

            if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
                log.info("❌ Username ya existe: {}", registerRequest.getUsername());
                return new AuthResponse(false, "El nombre de usuario ya está en uso");
            }

            if (usuarioRepository.existsByIdPersonaAndTipoUsuario(
                    registerRequest.getIdPersona(), registerRequest.getTipoUsuario())) {
                log.info("❌ Ya existe un usuario para esta persona y tipo: {}", registerRequest.getIdPersona());
                return new AuthResponse(false, "Ya existe un usuario para esta persona con el tipo especificado");
            }

            String idUsuario = generarIdUsuario();

            // Hashear contraseña con BCrypt
            String rawPassword = registerRequest.getPassword();
            String passwordHash = passwordEncoder.encode(rawPassword);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuario);
            usuario.setUsername(registerRequest.getUsername());
            usuario.setPasswordHash(passwordHash);
            usuario.setTipoUsuario(registerRequest.getTipoUsuario());
            usuario.setIdPersona(registerRequest.getIdPersona());
            usuario.setEstatus(EstatusUsuario.Activo);
            usuario.setIntentosLogin(0);
            usuario.setFechaCreacion(LocalDateTime.now());
            // Si viene especificado debeCambiarPassword, usarlo; de lo contrario por defecto true si fue creado por admin/recep
            usuario.setDebeCambiarPassword(registerRequest.getDebeCambiarPassword() != null ? registerRequest.getDebeCambiarPassword() : true);

            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            log.info("✅ Usuario guardado con BCrypt: {}", idUsuario);

            asignarRolPorDefecto(usuarioGuardado);

            AuthResponse response = obtenerInformacionUsuarioCompleta(usuarioGuardado);
            response.setSuccess(true);
            response.setMessage("Usuario registrado exitosamente");

            return response;

        } catch (Exception e) {
            log.error("❌ Error durante el registro: {}", e.getMessage(), e);
            return new AuthResponse(false, "Error durante el registro: " + e.getMessage());
        }
    }

    // ==================== ASIGNAR ROL POR DEFECTO ====================

    private boolean asignarRolPorDefecto(Usuario usuario) {
        try {
            Optional<Rol> rolOpt = rolRepository.findByNombreRolIgnoreCase(usuario.getTipoUsuario());
            if (rolOpt.isEmpty()) {
                rolOpt = rolRepository.findByNombreRol(usuario.getTipoUsuario());
            }
            if (rolOpt.isEmpty()) {
                rolOpt = rolRepository.findRolPorDefecto(usuario.getTipoUsuario());
            }

            if (rolOpt.isPresent()) {
                Rol rol = rolOpt.get();

                UsuarioRol usuarioRol = new UsuarioRol();
                usuarioRol.setIdUsuario(usuario.getIdUsuario());
                usuarioRol.setTipoUsuario(usuario.getTipoUsuario());
                usuarioRol.setIdRol(rol.getIdRol());
                usuarioRol.setEstatus(EstatusPersona.Activo);

                usuarioRolRepository.save(usuarioRol);
                log.info("✅ Rol asignado: {} para usuario: {}", rol.getNombreRol(), usuario.getUsername());
                return true;
            }

            log.error("❌ No se encontró rol por defecto para tipo: {}", usuario.getTipoUsuario());
            return false;

        } catch (Exception e) {
            log.error("❌ Error al asignar rol por defecto: {}", e.getMessage());
            return false;
        }
    }

    // ==================== GENERACIÓN DE ID DE USUARIO ====================

    private String generarIdUsuario() {
        try {
            String ultimoId = usuarioRepository.findUltimoIdUsuario();

            if (ultimoId == null) {
                return "USR001";
            }

            if (ultimoId.startsWith("USR")) {
                try {
                    String numeroStr = ultimoId.substring(3);
                    int numero = Integer.parseInt(numeroStr);
                    return String.format("USR%03d", numero + 1);
                } catch (NumberFormatException e) {
                    log.error("⚠ ID con formato inválido: {}", ultimoId);
                }
            }

            long totalUsuarios = usuarioRepository.count();
            return String.format("USR%03d", totalUsuarios + 1);

        } catch (Exception e) {
            long totalUsuarios = usuarioRepository.count();
            return String.format("USR%03d", totalUsuarios + 1);
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

            Usuario usuario = usuarioOpt.get();
            // Encriptar siempre con BCrypt
            usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
            usuario.setDebeCambiarPassword(false);
            usuario.setIntentosLogin(0);
            usuario.setFechaBloqueo(null);
            usuarioRepository.save(usuario);

            response.put("success", true);
            response.put("message", "Contraseña actualizada exitosamente");
            log.info("✅ Contraseña cambiada y encriptada con BCrypt para usuario: {}", username);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar contraseña: " + e.getMessage());
            log.error("❌ Error al cambiar contraseña: {}", e.getMessage());
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

            Usuario usuario = usuarioOpt.get();
            usuario.setEstatus(EstatusUsuario.Activo);
            usuario.setFechaBloqueo(null);
            usuario.setIntentosLogin(0);
            usuarioRepository.save(usuario);

            response.put("success", true);
            response.put("message", "Usuario desbloqueado exitosamente");
            log.info("✅ Usuario desbloqueado: {}", username);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al desbloquear usuario: " + e.getMessage());
            log.error("❌ Error al desbloquear usuario: {}", e.getMessage());
        }

        return response;
    }

    // ==================== OBTENER USUARIO POR USERNAME ====================

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}
