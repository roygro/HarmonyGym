package com.example.harmonyGymBack.controller;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.TipoMembresia;
import com.example.harmonyGymBack.service.MembresiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membresias")
@CrossOrigin(origins = "*")
public class MembresiaController {

    @Autowired
    private MembresiaService membresiaService;

    // CREAR nueva membresía
    @PostMapping
    public ResponseEntity<?> crearMembresia(@RequestBody Membresia membresia) {
        try {
            Membresia nuevaMembresia = membresiaService.crearMembresia(membresia);
            return new ResponseEntity<>(nuevaMembresia, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // OBTENER todas las membresías
    @GetMapping
    public ResponseEntity<List<Membresia>> obtenerTodasLasMembresias() {
        List<Membresia> membresias = membresiaService.obtenerTodasLasMembresias();
        return new ResponseEntity<>(membresias, HttpStatus.OK);
    }

    // OBTENER membresía por ID - CAMBIADO A String
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMembresiaPorId(@PathVariable String id) {
        try {
            Membresia membresia = membresiaService.obtenerMembresiaPorId(id);
            return new ResponseEntity<>(membresia, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membresía no encontrada");
        }
    }

    // OBTENER membresías activas
    @GetMapping("/activas")
    public ResponseEntity<List<Membresia>> obtenerMembresiasActivas() {
        List<Membresia> membresiasActivas = membresiaService.obtenerMembresiasActivas();
        return new ResponseEntity<>(membresiasActivas, HttpStatus.OK);
    }

    // OBTENER membresías por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Membresia>> obtenerMembresiasPorTipo(@PathVariable TipoMembresia tipo) {
        List<Membresia> membresias = membresiaService.obtenerMembresiasPorTipo(tipo);
        return new ResponseEntity<>(membresias, HttpStatus.OK);
    }

    // OBTENER membresías por precio máximo
    @GetMapping("/precio/{precioMaximo}")
    public ResponseEntity<List<Membresia>> obtenerMembresiasPorPrecioMaximo(@PathVariable Double precioMaximo) {
        List<Membresia> membresias = membresiaService.obtenerMembresiasPorPrecioMaximo(precioMaximo);
        return new ResponseEntity<>(membresias, HttpStatus.OK);
    }

    // OBTENER membresías por duración mínima
    @GetMapping("/duracion/{duracionMinima}")
    public ResponseEntity<List<Membresia>> obtenerMembresiasPorDuracionMinima(@PathVariable Integer duracionMinima) {
        List<Membresia> membresias = membresiaService.obtenerMembresiasPorDuracionMinima(duracionMinima);
        return new ResponseEntity<>(membresias, HttpStatus.OK);
    }

    // ACTUALIZAR membresía - CAMBIADO A String
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMembresia(@PathVariable String id, @RequestBody Membresia membresia) {
        try {
            Membresia membresiaActualizada = membresiaService.actualizarMembresia(id, membresia);
            return new ResponseEntity<>(membresiaActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }

    // DESACTIVAR membresía - CAMBIADO A String
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarMembresia(@PathVariable String id) {
        try {
            Membresia membresia = membresiaService.desactivarMembresia(id);
            return new ResponseEntity<>(membresia, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membresía no encontrada");
        }
    }

    // ACTIVAR membresía - CAMBIADO A String
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarMembresia(@PathVariable String id) {
        try {
            Membresia membresia = membresiaService.activarMembresia(id);
            return new ResponseEntity<>(membresia, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membresía no encontrada");
        }
    }

    // BUSCAR membresías por beneficio
    @GetMapping("/buscar")
    public ResponseEntity<List<Membresia>> buscarMembresiasPorBeneficio(@RequestParam String beneficio) {
        List<Membresia> membresias = membresiaService.buscarMembresiasPorBeneficio(beneficio);
        return new ResponseEntity<>(membresias, HttpStatus.OK);
    }

    // OBTENER estadísticas
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Object estadisticas = membresiaService.obtenerEstadisticas();
            return new ResponseEntity<>(estadisticas, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener estadísticas");
        }
    }
}