package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.model.enums.EstatusUsuario;
import com.example.harmonyGymBack.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final PlanPagoRepository planPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final AdministradorRepository administradorRepository;
    private final RecepcionistaRepository recepcionistaRepository;
    private final InstructorRepository instructorRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           PlanPagoRepository planPagoRepository,
                           UsuarioRepository usuarioRepository,
                           UsuarioRolRepository usuarioRolRepository,
                           AdministradorRepository administradorRepository,
                           RecepcionistaRepository recepcionistaRepository,
                           InstructorRepository instructorRepository,
                           ClienteRepository clienteRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.planPagoRepository = planPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.administradorRepository = administradorRepository;
        this.recepcionistaRepository = recepcionistaRepository;
        this.instructorRepository = instructorRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        inicializarRoles();
        inicializarPlanesPago();
        inicializarUsuariosPrueba();
    }

    private void inicializarRoles() {
        crearRolSiNoExiste("ROL001", "Administrador", "Acceso administrativo general al sistema", "[\"ALL\"]");
        crearRolSiNoExiste("ROL002", "Recepcionista", "Acceso a ventas, clientes y asistencias", "[\"RECEPCION\"]");
        crearRolSiNoExiste("ROL003", "Instructor", "Acceso a clases, rutinas y calificaciones", "[\"INSTRUCTOR\"]");
        crearRolSiNoExiste("ROL004", "Cliente", "Acceso a perfil, asistencias y rutinas asignadas", "[\"CLIENTE\"]");
    }

    private void crearRolSiNoExiste(String idRol, String nombreRol, String descripcion, String permisos) {
        Optional<Rol> rolPorId = rolRepository.findById(idRol);
        Optional<Rol> rolPorNombre = rolRepository.findByNombreRolIgnoreCase(nombreRol);

        if (rolPorId.isEmpty() && rolPorNombre.isEmpty()) {
            Rol nuevo = new Rol(idRol, nombreRol, descripcion, permisos);
            rolRepository.save(nuevo);
            log.info("Rol inicial creado: {} ({})", nombreRol, idRol);
        }
    }

    private void inicializarPlanesPago() {
        if (planPagoRepository.count() == 0) {
            PlanMensual mensual = new PlanMensual();
            mensual.setNombre("Plan Mensual");
            mensual.setDescripcion("Pago mensual estándar");
            mensual.setDuracionDias(30);
            mensual.setFactorDescuento(1.0);
            mensual.setEstatus(EstatusPersona.Activo);
            planPagoRepository.save(mensual);

            PlanTrimestral trimestral = new PlanTrimestral();
            trimestral.setNombre("Plan Trimestral");
            trimestral.setDescripcion("3 meses con 10% de descuento");
            trimestral.setDuracionDias(90);
            trimestral.setFactorDescuento(0.90);
            trimestral.setEstatus(EstatusPersona.Activo);
            planPagoRepository.save(trimestral);

            PlanSemestral semestral = new PlanSemestral();
            semestral.setNombre("Plan Semestral");
            semestral.setDescripcion("6 meses con 15% de descuento");
            semestral.setDuracionDias(180);
            semestral.setFactorDescuento(0.85);
            semestral.setEstatus(EstatusPersona.Activo);
            planPagoRepository.save(semestral);

            PlanAnual anual = new PlanAnual();
            anual.setNombre("Plan Anual");
            anual.setDescripcion("12 meses con 20% de descuento");
            anual.setDuracionDias(365);
            anual.setFactorDescuento(0.80);
            anual.setEstatus(EstatusPersona.Activo);
            planPagoRepository.save(anual);

            log.info("Planes de pago estándar inicializados correctamente (Mensual, Trimestral, Semestral, Anual)");
        }
    }

    private void inicializarUsuariosPrueba() {
        // 1. admin / Admin123
        asegurarUsuario("admin", "Admin123", "Administrador", "ADM001", "Administrador General");

        // 2. recepcion / Recep123
        asegurarUsuario("recepcion", "Recep123", "Recepcionista", "REC001", "Recepcionista Principal");

        // 3. instructor / Instr123
        asegurarUsuario("instructor", "Instr123", "Instructor", "INS001", "Instructor Principal");

        // 4. cliente / Cliente123
        asegurarUsuario("cliente", "Cliente123", "Cliente", "CLI001", "Cliente Ejemplo");
    }

    private void asegurarUsuario(String username, String rawPassword, String tipoUsuario, String folioPersona, String nombreCompleto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        Usuario usuario;

        if (usuarioOpt.isEmpty()) {
            asegurarPersona(tipoUsuario, folioPersona, nombreCompleto);

            usuario = new Usuario();
            usuario.setIdUsuario("USR-" + username.toUpperCase());
            usuario.setUsername(username);
            usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
            usuario.setTipoUsuario(tipoUsuario);
            usuario.setIdPersona(folioPersona);
            usuario.setEstatus(EstatusUsuario.Activo);
            usuario.setIntentosLogin(0);
            usuario.setDebeCambiarPassword(false);
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario = usuarioRepository.save(usuario);

            // Asociar rol
            Optional<Rol> rolOpt = rolRepository.findByNombreRolIgnoreCase(tipoUsuario);
            if (rolOpt.isPresent()) {
                UsuarioRol ur = new UsuarioRol();
                ur.setIdUsuario(usuario.getIdUsuario());
                ur.setIdRol(rolOpt.get().getIdRol());
                ur.setTipoUsuario(tipoUsuario);
                ur.setEstatus(EstatusPersona.Activo);
                usuarioRolRepository.save(ur);
            }
            log.info("Usuario de prueba creado: {} [{}]", username, tipoUsuario);
        } else {
            usuario = usuarioOpt.get();
            // Asegurar que la contraseña esté hasheada con el password de prueba
            if (!passwordEncoder.matches(rawPassword, usuario.getPasswordHash())) {
                usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
                usuario.setEstatus(EstatusUsuario.Activo);
                usuario.setIntentosLogin(0);
                usuarioRepository.save(usuario);
                log.info("Contraseña de prueba actualizada para usuario: {}", username);
            }
        }
    }

    private void asegurarPersona(String tipoUsuario, String folioPersona, String nombreCompleto) {
        switch (tipoUsuario) {
            case "Administrador":
                if (administradorRepository.findById(folioPersona).isEmpty()) {
                    Administrador a = new Administrador();
                    a.setFolioAdmin(folioPersona);
                    a.setNombreCom(nombreCompleto);
                    a.setFechaRegistro(LocalDateTime.now());
                    administradorRepository.save(a);
                }
                break;
            case "Recepcionista":
                if (recepcionistaRepository.findById(folioPersona).isEmpty()) {
                    Recepcionista r = new Recepcionista();
                    r.setIdRecepcionista(folioPersona);
                    r.setNombre(nombreCompleto);
                    r.setEmail(folioPersona.toLowerCase() + "@harmonygym.com");
                    r.setTelefono("4181234567");
                    r.setEstatus(EstatusPersona.Activo);
                    r.setFechaContratacion(LocalDate.now());
                    recepcionistaRepository.save(r);
                }
                break;
            case "Instructor":
                if (instructorRepository.findById(folioPersona).isEmpty()) {
                    Instructor i = new Instructor();
                    i.setFolioInstructor(folioPersona);
                    i.setNombre(nombreCompleto);
                    i.setEmail(folioPersona.toLowerCase() + "@harmonygym.com");
                    i.setEspecialidad("Fitness Integral");
                    i.setEstatus(EstatusPersona.Activo);
                    i.setFechaContratacion(LocalDate.now());
                    instructorRepository.save(i);
                }
                break;
            case "Cliente":
                if (clienteRepository.findById(folioPersona).isEmpty()) {
                    Cliente c = new Cliente();
                    c.setFolioCliente(folioPersona);
                    c.setNombre(nombreCompleto);
                    c.setEmail(folioPersona.toLowerCase() + "@cliente.com");
                    c.setTelefono("4189998877");
                    c.setEstatus(EstatusPersona.Activo);
                    c.setFechaRegistro(LocalDateTime.now());
                    clienteRepository.save(c);
                }
                break;
        }
    }
}
