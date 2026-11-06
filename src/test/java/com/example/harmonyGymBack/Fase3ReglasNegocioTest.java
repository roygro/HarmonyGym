package com.example.harmonyGymBack;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.*;
import com.example.harmonyGymBack.repository.*;
import com.example.harmonyGymBack.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Fase3ReglasNegocioTest {

    @Autowired
    private VencimientoMembresiasTask vencimientoTask;

    @Autowired
    private MembresiaClienteRepository membresiaClienteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private PlanPagoRepository planPagoRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ClienteActividadService clienteActividadService;

    @Autowired
    private RealizaActividadRepository realizaActividadRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private AdministradorServiceImpl administradorService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    private Cliente clienteTest;
    private Membresia membresiaTest;
    private PlanPago planTest;

    @BeforeEach
    void setUp() {
        Cliente c = new Cliente();
        c.setFolioCliente("CLI_F3_" + System.currentTimeMillis());
        c.setNombre("Cliente Fase 3");
        c.setEmail("cliente_f3_" + System.currentTimeMillis() + "@test.com");
        c.setEstatus(EstatusPersona.Activo);
        clienteTest = clienteRepository.save(c);
        if (!instructorRepository.existsById("INS001")) {
            Instructor inst = new Instructor();
            inst.setFolioInstructor("INS001");
            inst.setNombre("Instructor");
            inst.setApp("Prueba");
            inst.setEmail("ins001@test.com");
            inst.setEstatus(EstatusPersona.Activo);
            instructorRepository.save(inst);
        }


        Membresia m = new Membresia();
        m.setIdMembresia("MEM_F3_" + System.currentTimeMillis());
        m.setTipo(TipoMembresia.Básica);
        m.setPrecio(500.0);
        m.setDuracion(30);
        m.setEstatus(EstatusCatalogo.Activa);
        membresiaTest = membresiaRepository.save(m);

        planTest = planPagoRepository.findAll().stream().findFirst().orElseGet(() -> {
            PlanMensual p = new PlanMensual();
            return planPagoRepository.save(p);
        });
    }

    @Test
    @DisplayName("3.1 Membresía vencida pasa a Expirada con la tarea programada")
    void testVencimientoAutomaticoMembresias() {
        MembresiaCliente mc = new MembresiaCliente();
        mc.setCliente(clienteTest);
        mc.setMembresia(membresiaTest);
        mc.setPlanPago(planTest);
        mc.setFechaInicio(LocalDate.now().minusDays(35));
        mc.setFechaFin(LocalDate.now().minusDays(5));
        mc.setEstatus(EstatusMembresiaCliente.Activa);
        final MembresiaCliente mcGuardada = membresiaClienteRepository.save(mc);

        assertFalse(mcGuardada.isVigente(), "isVigente() debe ser false si fechaFin < hoy");

        vencimientoTask.expirarMembresiasVencidas();

        MembresiaCliente actualizado = membresiaClienteRepository.findById(mcGuardada.getIdMembresiaCliente()).orElseThrow();
        assertEquals(EstatusMembresiaCliente.Expirada, actualizado.getEstatus(), "Debe marcarse como Expirada");
    }

    @Test
    @DisplayName("3.2 Clases: validación de cupo, membresía requerida y no doble inscripción")
    void testInscripcionActividadesYCupo() {
        final String folioCli1 = clienteTest.getFolioCliente();

        MembresiaCliente mc = new MembresiaCliente();
        mc.setCliente(clienteTest);
        mc.setMembresia(membresiaTest);
        mc.setPlanPago(planTest);
        mc.setFechaInicio(LocalDate.now());
        mc.setFechaFin(LocalDate.now().plusDays(30));
        mc.setEstatus(EstatusMembresiaCliente.Activa);
        membresiaClienteRepository.save(mc);

        Actividad act = new Actividad();
        act.setIdActividad("ACT_T1_" + System.currentTimeMillis());
        act.setNombreActividad("Spinning Express");
        act.setFechaActividad(LocalDate.now().plusDays(2));
        act.setHoraInicio(LocalTime.of(10, 0));
        act.setHoraFin(LocalTime.of(11, 0));
        act.setCupo(1);
        act.setCupoMaximo(1);
        act.setLugar("Sala Spinning");
        act.setFolioInstructor("INS001");
        act.setEstatus(EstatusCatalogo.Activa);
        final Actividad actGuardada = actividadRepository.save(act);
        final String idAct = actGuardada.getIdActividad();

        RealizaActividad inscripcion = clienteActividadService.inscribirClienteEnActividad(
                folioCli1, idAct
        );
        assertNotNull(inscripcion);
        assertEquals(EstatusInscripcion.Inscrito, inscripcion.getEstatus());

        // Intentar inscribirse de nuevo a la misma clase -> Error
        assertThrows(RuntimeException.class, () -> {
            clienteActividadService.inscribirClienteEnActividad(folioCli1, idAct);
        });

        // Intentar inscribir un segundo cliente cuando el cupo era 1 -> Error Sin Cupo
        Cliente c2 = new Cliente();
        c2.setFolioCliente("CLI_F3_2_" + System.currentTimeMillis());
        c2.setNombre("Cliente 2");
        c2.setEstatus(EstatusPersona.Activo);
        final Cliente cliente2 = clienteRepository.save(c2);
        final String folioCli2 = cliente2.getFolioCliente();

        MembresiaCliente mc2 = new MembresiaCliente();
        mc2.setCliente(cliente2);
        mc2.setMembresia(membresiaTest);
        mc2.setPlanPago(planTest);
        mc2.setFechaInicio(LocalDate.now());
        mc2.setFechaFin(LocalDate.now().plusDays(30));
        mc2.setEstatus(EstatusMembresiaCliente.Activa);
        membresiaClienteRepository.save(mc2);

        assertThrows(RuntimeException.class, () -> {
            clienteActividadService.inscribirClienteEnActividad(folioCli2, idAct);
        });
    }

    @Test
    @DisplayName("3.2 Asistencia del instructor y calificación de 1 a 5")
    void testAsistenciaYCalificacion() {
        final String folioCli = clienteTest.getFolioCliente();

        MembresiaCliente mc = new MembresiaCliente();
        mc.setCliente(clienteTest);
        mc.setMembresia(membresiaTest);
        mc.setPlanPago(planTest);
        mc.setFechaInicio(LocalDate.now());
        mc.setFechaFin(LocalDate.now().plusDays(30));
        mc.setEstatus(EstatusMembresiaCliente.Activa);
        membresiaClienteRepository.save(mc);

        Actividad act = new Actividad();
        act.setIdActividad("ACT_T2_" + System.currentTimeMillis());
        act.setNombreActividad("Yoga Matutino");
        act.setFechaActividad(LocalDate.now().plusDays(1));
        act.setHoraInicio(LocalTime.of(8, 0));
        act.setHoraFin(LocalTime.of(9, 0));
        act.setCupo(10);
        act.setCupoMaximo(10);
        act.setLugar("Sala Yoga");
        act.setFolioInstructor("INS001");
        act.setEstatus(EstatusCatalogo.Activa);
        final Actividad actGuardada = actividadRepository.save(act);
        final String idAct = actGuardada.getIdActividad();

        clienteActividadService.inscribirClienteEnActividad(folioCli, idAct);

        // Instructor no autorizado intenta registrar asistencia -> Error
        assertThrows(RuntimeException.class, () -> {
            clienteActividadService.registrarAsistencia(idAct, "INS999", List.of());
        });

        // Instructor correcto registra asistencia
        Map<String, Object> item = new HashMap<>();
        item.put("folioCliente", folioCli);
        item.put("asistio", true);
        var res = clienteActividadService.registrarAsistencia(idAct, "INS001", List.of(item));
        assertFalse(res.isEmpty());
        assertTrue(res.get(0).getAsistio());
        assertEquals(EstatusInscripcion.Completado, res.get(0).getEstatus());

        // Calificación válida
        RealizaActividad calificado = clienteActividadService.calificarActividad(
                folioCli, idAct, 5, "Excelente clase"
        );
        assertEquals(5, calificado.getCalificacion());
        assertEquals("Excelente clase", calificado.getComentarios());

        // Calificación fuera de rango -> Error
        assertThrows(RuntimeException.class, () -> {
            clienteActividadService.calificarActividad(folioCli, idAct, 6, "Invalido");
        });
    }

    @Test
    @DisplayName("3.3 Protección del último administrador activo")
    void testProteccionUltimoAdministrador() {
        administradorRepository.deleteAll();

        Administrador unico = new Administrador();
        unico.setFolioAdmin("ADM001");
        unico.setNombreCom("Admin");
        administradorRepository.save(unico);

        // Intentar eliminar al único admin debe fallar
        assertThrows(RuntimeException.class, () -> {
            administradorService.eliminarAdministrador("ADM001");
        });

        // Intentar auto-eliminarse debe fallar
        Administrador segundo = new Administrador();
        segundo.setFolioAdmin("ADM002");
        segundo.setNombreCom("Admin");
        administradorRepository.save(segundo);

        assertThrows(RuntimeException.class, () -> {
            administradorService.eliminarAdministrador("ADM002", "ADM002");
        });
    }

    @Test
    @DisplayName("3.4 Datos iniciales: roles ROL001 a ROL004 existen")
    void testRolesIniciales() {
        assertTrue(rolRepository.findById("ROL001").isPresent(), "ROL001 debe existir");
        assertTrue(rolRepository.findById("ROL002").isPresent(), "ROL002 debe existir");
        assertTrue(rolRepository.findById("ROL003").isPresent(), "ROL003 debe existir");
        assertTrue(rolRepository.findById("ROL004").isPresent(), "ROL004 debe existir");
    }
}
