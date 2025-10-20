package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstructorService {

    @Autowired
    private InstructorRepository instructorRepository;

    public List<Instructor> getAllInstructores() {
        return instructorRepository.findAll();
    }

    public Optional<Instructor> getInstructorById(String folioInstructor) {
        return instructorRepository.findById(folioInstructor);
    }

    public Instructor createInstructor(Instructor instructor) {
        if (instructorRepository.existsById(instructor.getFolioInstructor())) {
            throw new RuntimeException("El folio de instructor ya existe");
        }

        // Validar que no exista un instructor con el mismo nombre completo
        if (instructorRepository.existsByNombreCompletoAndNotId(
                instructor.getNombre(), instructor.getApp(), instructor.getApm(), "")) {
            throw new RuntimeException("Ya existe un instructor con el mismo nombre completo");
        }

        return instructorRepository.save(instructor);
    }

    public Instructor updateInstructor(String folioInstructor, Instructor instructorDetails) {
        Optional<Instructor> optionalInstructor = instructorRepository.findById(folioInstructor);

        if (optionalInstructor.isPresent()) {
            Instructor instructor = optionalInstructor.get();

            if (instructorRepository.existsByNombreCompletoAndNotId(
                    instructorDetails.getNombre(), instructorDetails.getApp(),
                    instructorDetails.getApm(), folioInstructor)) {
                throw new RuntimeException("Ya existe otro instructor con el mismo nombre completo");
            }

            instructor.setNombre(instructorDetails.getNombre());
            instructor.setApp(instructorDetails.getApp());
            instructor.setApm(instructorDetails.getApm());
            instructor.setHoraEntrada(instructorDetails.getHoraEntrada());
            instructor.setHoraSalida(instructorDetails.getHoraSalida());
            instructor.setEspecialidad(instructorDetails.getEspecialidad());
            instructor.setFechaContratacion(instructorDetails.getFechaContratacion());

            return instructorRepository.save(instructor);
        } else {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }
    }

    public void deleteInstructor(String folioInstructor) {
        if (instructorRepository.existsById(folioInstructor)) {
            instructorRepository.deleteById(folioInstructor);
        } else {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }
    }

    public void darDeBajaInstructor(String folioInstructor) {
        Optional<Instructor> optionalInstructor = instructorRepository.findById(folioInstructor);

        if (optionalInstructor.isPresent()) {
            instructorRepository.updateEstatus(folioInstructor, "Inactivo");
        } else {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }
    }

    public void activarInstructor(String folioInstructor) {
        Optional<Instructor> optionalInstructor = instructorRepository.findById(folioInstructor);

        if (optionalInstructor.isPresent()) {
            instructorRepository.updateEstatus(folioInstructor, "Activo");
        } else {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }
    }

    public List<Instructor> searchInstructores(String searchTerm) {
        return instructorRepository.findByNombreContainingIgnoreCase(searchTerm);
    }

    public List<Instructor> getInstructoresByEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    public List<Instructor> getInstructoresByEstatus(String estatus) {
        return instructorRepository.findByEstatus(estatus);
    }
}