package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Administrador;
import com.example.harmonyGymBack.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @GetMapping
    public List<Administrador> getAllAdministradores() {
        return administradorService.getAllAdministradores();
    }

    @GetMapping("/{folioAdmin}")
    public ResponseEntity<Administrador> getAdministradorById(@PathVariable String folioAdmin) {
        Optional<Administrador> administrador = administradorService.getAdministradorById(folioAdmin);
        return administrador.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAdministrador(@RequestBody Administrador administrador) {
        try {
            Administrador nuevoAdministrador = administradorService.createAdministrador(administrador);
            return ResponseEntity.ok(nuevoAdministrador);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioAdmin}")
    public ResponseEntity<?> updateAdministrador(@PathVariable String folioAdmin, @RequestBody Administrador administradorDetails) {
        try {
            Administrador administradorActualizado = administradorService.updateAdministrador(folioAdmin, administradorDetails);
            return ResponseEntity.ok(administradorActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{folioAdmin}")
    public ResponseEntity<?> deleteAdministrador(@PathVariable String folioAdmin) {
        try {
            administradorService.deleteAdministrador(folioAdmin);
            return ResponseEntity.ok().body("Administrador eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Administrador> searchAdministradores(@RequestParam String searchTerm) {
        return administradorService.searchAdministradores(searchTerm);
    }

    @GetMapping("/search/app")
    public List<Administrador> searchByApp(@RequestParam String app) {
        return administradorService.searchByApp(app);
    }

    @GetMapping("/search/apm")
    public List<Administrador> searchByApm(@RequestParam String apm) {
        return administradorService.searchByApm(apm);
    }
}