package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.*;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.repository.PlanPagoRepository;
import com.example.harmonyGymBack.repository.RolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PlanPagoRepository planPagoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        inicializarRoles();
        inicializarPlanesPago();
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
}
