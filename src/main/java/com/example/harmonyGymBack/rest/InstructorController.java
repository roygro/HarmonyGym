package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.service.InstructorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instructores")
@CrossOrigin(origins = "*")
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    @GetMapping
    public List<Instructor> getAllInstructores() {
        return instructorService.getAllInstructores();
    }

    @GetMapping("/{folioInstructor}")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable String folioInstructor) {
        Optional<Instructor> instructor = instructorService.getInstructorById(folioInstructor);
        return instructor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createInstructor(@RequestBody Instructor instructor) {
        try {
            Instructor nuevoInstructor = instructorService.createInstructor(instructor);
            return ResponseEntity.ok(nuevoInstructor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioInstructor}")
    public ResponseEntity<?> updateInstructor(@PathVariable String folioInstructor, @RequestBody Instructor instructorDetails) {
        try {
            Instructor instructorActualizado = instructorService.updateInstructor(folioInstructor, instructorDetails);
            return ResponseEntity.ok(instructorActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{folioInstructor}")
    public ResponseEntity<?> deleteInstructor(@PathVariable String folioInstructor) {
        try {
            instructorService.deleteInstructor(folioInstructor);
            return ResponseEntity.ok().body("Instructor eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioInstructor}/baja")
    public ResponseEntity<?> darDeBajaInstructor(@PathVariable String folioInstructor) {
        try {
            instructorService.darDeBajaInstructor(folioInstructor);
            return ResponseEntity.ok().body("Instructor dado de baja correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioInstructor}/activar")
    public ResponseEntity<?> activarInstructor(@PathVariable String folioInstructor) {
        try {
            instructorService.activarInstructor(folioInstructor);
            return ResponseEntity.ok().body("Instructor activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Instructor> searchInstructores(@RequestParam String searchTerm) {
        return instructorService.searchInstructores(searchTerm);
    }

    @GetMapping("/especialidad/{especialidad}")
    public List<Instructor> getInstructoresByEspecialidad(@PathVariable String especialidad) {
        return instructorService.getInstructoresByEspecialidad(especialidad);
    }

    @GetMapping("/estatus/{estatus}")
    public List<Instructor> getInstructoresByEstatus(@PathVariable String estatus) {
        return instructorService.getInstructoresByEstatus(estatus);
    }
}