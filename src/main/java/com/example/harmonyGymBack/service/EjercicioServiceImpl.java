package com.example.harmonyGymBack.service;
import com.example.harmonyGymBack.model.EjercicioEntity;
import com.example.harmonyGymBack.repository.EjercicioRepository;
import com.example.harmonyGymBack.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;



@Service
public class EjercicioServiceImpl {

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    public List<EjercicioEntity> findAll() {
        return ejercicioRepository.findAll();
    }

    public Optional<EjercicioEntity> findByIdEjercicio(String idEjercicio) {
        return ejercicioRepository.findByIdEjercicio(idEjercicio);
    }

    public List<EjercicioEntity> findByGrupoMuscular(String grupoMuscular) {
        return ejercicioRepository.findByGrupoMuscular(grupoMuscular);
    }

    public List<EjercicioEntity> findByInstructor(String folioInstructor) {
        return ejercicioRepository.findByFolioInstructor(folioInstructor);
    }

    public EjercicioEntity createEjercicio(EjercicioEntity ejercicio) {
        // Verificar que el instructor existe
        if (ejercicio.getFolioInstructor() != null &&
                !instructorRepository.existsById(ejercicio.getFolioInstructor())) {
            throw new RuntimeException("El instructor no existe: " + ejercicio.getFolioInstructor());
        }

        // Generar ID automático si no viene
        if (ejercicio.getIdEjercicio() == null || ejercicio.getIdEjercicio().isEmpty()) {
            String id = generarIdEjercicio();
            ejercicio.setIdEjercicio(id);
        }

        return ejercicioRepository.save(ejercicio);
    }

    public EjercicioEntity updateEjercicio(String idEjercicio, EjercicioEntity ejercicioActualizado) {
        Optional<EjercicioEntity> ejercicioExistente = ejercicioRepository.findByIdEjercicio(idEjercicio);

        if (ejercicioExistente.isPresent()) {
            EjercicioEntity ejercicio = ejercicioExistente.get();

            ejercicio.setNombre(ejercicioActualizado.getNombre());
            ejercicio.setDescripcion(ejercicioActualizado.getDescripcion());
            ejercicio.setTiempo(ejercicioActualizado.getTiempo());
            ejercicio.setSeries(ejercicioActualizado.getSeries());
            ejercicio.setRepeticiones(ejercicioActualizado.getRepeticiones());
            ejercicio.setDescanso(ejercicioActualizado.getDescanso());
            ejercicio.setDuracionEstimada(ejercicioActualizado.getDuracionEstimada());
            ejercicio.setEstatus(ejercicioActualizado.getEstatus());
            ejercicio.setGrupoMuscular(ejercicioActualizado.getGrupoMuscular());
            ejercicio.setEquipoNecesario(ejercicioActualizado.getEquipoNecesario());
            ejercicio.setInstrucciones(ejercicioActualizado.getInstrucciones());
            ejercicio.setFolioInstructor(ejercicioActualizado.getFolioInstructor());

            return ejercicioRepository.save(ejercicio);
        }

        return null;
    }

    public boolean deleteEjercicio(String idEjercicio) {
        Optional<EjercicioEntity> ejercicio = ejercicioRepository.findByIdEjercicio(idEjercicio);

        if (ejercicio.isPresent()) {
            // Cambiar estatus a Inactivo en lugar de eliminar
            EjercicioEntity ej = ejercicio.get();
            ej.setEstatus("Inactivo");
            ejercicioRepository.save(ej);
            return true;
        }

        return false;
    }

    private String generarIdEjercicio() {
        Optional<Integer> maxFolio = ejercicioRepository.findMaxFolioNumber();
        int nextNumber = maxFolio.map(n -> n + 1).orElse(1);
        return String.format("EJE%03d", nextNumber);
    }
}