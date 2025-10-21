package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.service.RecepcionistaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recepcionistas")
@CrossOrigin(origins = "*")
public class RecepcionistaController {

    @Autowired
    private RecepcionistaServiceImpl recepcionistaService;

    @GetMapping
    public List<Recepcionista> getAllRecepcionistas() {
        return recepcionistaService.getAllRecepcionistas();
    }

    @GetMapping("/{idRecepcionista}")
    public ResponseEntity<Recepcionista> getRecepcionistaById(@PathVariable String idRecepcionista) {
        Optional<Recepcionista> recepcionista = recepcionistaService.getRecepcionistaById(idRecepcionista);
        return recepcionista.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRecepcionista(@RequestBody Recepcionista recepcionista) {
        try {
            Recepcionista nuevoRecepcionista = recepcionistaService.createRecepcionista(recepcionista);
            return ResponseEntity.ok(nuevoRecepcionista);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{idRecepcionista}")
    public ResponseEntity<?> updateRecepcionista(@PathVariable String idRecepcionista, @RequestBody Recepcionista recepcionistaDetails) {
        try {
            Recepcionista recepcionistaActualizado = recepcionistaService.updateRecepcionista(idRecepcionista, recepcionistaDetails);
            return ResponseEntity.ok(recepcionistaActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{idRecepcionista}")
    public ResponseEntity<?> deleteRecepcionista(@PathVariable String idRecepcionista) {
        try {
            recepcionistaService.deleteRecepcionista(idRecepcionista);
            return ResponseEntity.ok().body("Recepcionista eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{idRecepcionista}/baja")
    public ResponseEntity<?> darDeBajaRecepcionista(@PathVariable String idRecepcionista) {
        try {
            recepcionistaService.darDeBajaRecepcionista(idRecepcionista);
            return ResponseEntity.ok().body("Recepcionista dado de baja correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{idRecepcionista}/activar")
    public ResponseEntity<?> activarRecepcionista(@PathVariable String idRecepcionista) {
        try {
            recepcionistaService.activarRecepcionista(idRecepcionista);
            return ResponseEntity.ok().body("Recepcionista activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Recepcionista> searchRecepcionistas(@RequestParam String searchTerm) {
        return recepcionistaService.searchRecepcionistas(searchTerm);
    }

    @GetMapping("/estatus/{estatus}")
    public List<Recepcionista> getRecepcionistasByEstatus(@PathVariable String estatus) {
        return recepcionistaService.getRecepcionistasByEstatus(estatus);
    }
}