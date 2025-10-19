package com.example.harmonyGymBack.controller;

import com.example.harmonyGymBack.model.Membresia;
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

    @PostMapping
    public ResponseEntity<Membresia> crearMembresia(@RequestBody Membresia membresia) {
        Membresia nuevaMembresia = membresiaService.crearMembresia(membresia);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMembresia);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Membresia>> obtenerMembresiasCliente(@PathVariable Long clienteId) {
        List<Membresia> membresias = membresiaService.obtenerMembresiasPorCliente(clienteId);
        return ResponseEntity.ok(membresias);
    }
}