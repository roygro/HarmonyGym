package com.example.harmonyGymBack.rest;
import com.example.harmonyGymBack.model.EjercicioEntity;
import com.example.harmonyGymBack.service.EjercicioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ejercicios")
@CrossOrigin(origins = "*")
public class EjercicioController {

    @Autowired
    private EjercicioServiceImpl ejercicioService;

    @GetMapping
    public List<EjercicioEntity> getAllEjercicios() {
        return ejercicioService.findAll();
    }

    @GetMapping("/{idEjercicio}")
    public ResponseEntity<EjercicioEntity> getEjercicioById(@PathVariable String idEjercicio) {
        return ejercicioService.findByIdEjercicio(idEjercicio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo-muscular/{grupoMuscular}")
    public List<EjercicioEntity> getEjerciciosByGrupoMuscular(@PathVariable String grupoMuscular) {
        return ejercicioService.findByGrupoMuscular(grupoMuscular);
    }

    @PostMapping
    public EjercicioEntity createEjercicio(@RequestBody EjercicioEntity ejercicio) {
        return ejercicioService.createEjercicio(ejercicio);
    }

    @PutMapping("/{idEjercicio}")
    public ResponseEntity<EjercicioEntity> updateEjercicio(@PathVariable String idEjercicio, @RequestBody EjercicioEntity ejercicio) {
        EjercicioEntity ejercicioActualizado = ejercicioService.updateEjercicio(idEjercicio, ejercicio);
        return ejercicioActualizado != null ? ResponseEntity.ok(ejercicioActualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{idEjercicio}")
    public ResponseEntity<Void> deleteEjercicio(@PathVariable String idEjercicio) {
        boolean deleted = ejercicioService.deleteEjercicio(idEjercicio);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    @GetMapping("/instructor/{folioInstructor}")
    public List<EjercicioEntity> getEjerciciosByInstructor(@PathVariable String folioInstructor) {
        return ejercicioService.findByInstructor(folioInstructor);
    }
}