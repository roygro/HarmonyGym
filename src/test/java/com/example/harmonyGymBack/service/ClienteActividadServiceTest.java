package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.*;
import com.example.harmonyGymBack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteActividadServiceTest {

    @Mock
    private RealizaActividadRepository realizaActividadRepository;
    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private MembresiaClienteRepository membresiaClienteRepository;
    @Mock
    private ActividadServiceImpl actividadService;

    @InjectMocks
    private ClienteActividadService clienteActividadService;

    private Actividad actividad;
    private Cliente cliente;
    private MembresiaCliente membresiaCliente;

    @BeforeEach
    void setUp() {
        actividad = new Actividad();
        actividad.setIdActividad("ACT001");
        actividad.setNombreActividad("CrossFit");
        actividad.setEstatus(EstatusCatalogo.Activa);
        actividad.setFechaActividad(LocalDate.now().plusDays(2));
        actividad.setHoraInicio(LocalTime.of(10, 0));
        actividad.setHoraFin(LocalTime.of(11, 0));
        actividad.setCupoMaximo(5);

        cliente = new Cliente();
        cliente.setFolioCliente("CLI001");
        cliente.setNombre("Cliente Prueba");
        cliente.setEstatus(EstatusPersona.Activo);

        membresiaCliente = new MembresiaCliente();
        membresiaCliente.setIdMembresiaCliente(1L);
        membresiaCliente.setEstatus(EstatusMembresiaCliente.Activa);
        membresiaCliente.setFechaInicio(LocalDate.now().minusDays(5));
        membresiaCliente.setFechaFin(LocalDate.now().plusDays(25));
    }

    @Test
    @DisplayName("Inscripción exitosa cuando hay cupo disponible")
    void testInscripcionExitosaConCupo() {
        when(actividadRepository.findByIdWithLock("ACT001")).thenReturn(Optional.of(actividad));
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.of(membresiaCliente));
        // Cupo máximo es 5, inscripciones activas actuales: 2
        when(realizaActividadRepository.countInscripcionesActivasByActividad("ACT001")).thenReturn(2L);
        when(realizaActividadRepository.findInscripcionActiva("CLI001", "ACT001")).thenReturn(Optional.empty());
        when(realizaActividadRepository.findInscripcionesConActividad("CLI001")).thenReturn(Collections.emptyList());

        when(realizaActividadRepository.save(any(RealizaActividad.class))).thenAnswer(invocation -> {
            RealizaActividad ra = invocation.getArgument(0);
            ra.setIdRegistro(100L);
            return ra;
        });

        RealizaActividad resultado = clienteActividadService.inscribirClienteEnActividad("CLI001", "ACT001");

        assertNotNull(resultado);
        assertEquals(EstatusInscripcion.Inscrito, resultado.getEstatus());
        verify(realizaActividadRepository).save(any(RealizaActividad.class));
    }

    @Test
    @DisplayName("Inscripción rechazada cuando se alcanza el cupo máximo")
    void testInscripcionRechazadaPorCupoLleno() {
        when(actividadRepository.findByIdWithLock("ACT001")).thenReturn(Optional.of(actividad));
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.of(membresiaCliente));
        // Cupo máximo es 5, inscripciones activas actuales: 5 (lleno)
        when(realizaActividadRepository.countInscripcionesActivasByActividad("ACT001")).thenReturn(5L);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                clienteActividadService.inscribirClienteEnActividad("CLI001", "ACT001")
        );

        assertTrue(ex.getMessage().contains("Sin cupo disponible"));
        verify(realizaActividadRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inscripción rechazada si el cliente no tiene membresía activa vigente")
    void testInscripcionRechazadaSinMembresiaVigente() {
        when(actividadRepository.findByIdWithLock("ACT001")).thenReturn(Optional.of(actividad));
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        // Membresía vencida
        membresiaCliente.setFechaFin(LocalDate.now().minusDays(1));
        when(membresiaClienteRepository.findByClienteFolioClienteAndEstatus("CLI001", EstatusMembresiaCliente.Activa))
                .thenReturn(Optional.of(membresiaCliente));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                clienteActividadService.inscribirClienteEnActividad("CLI001", "ACT001")
        );

        assertTrue(ex.getMessage().contains("membresía"));
        verify(realizaActividadRepository, never()).save(any());
    }
}
