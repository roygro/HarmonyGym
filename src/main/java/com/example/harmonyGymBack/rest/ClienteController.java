package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteService.getAllClientes();
    }

    @GetMapping("/{folioCliente}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable String folioCliente) {
        Optional<Cliente> cliente = clienteService.getClienteById(folioCliente);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.createCliente(cliente);
            return ResponseEntity.ok(nuevoCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioCliente}")
    public ResponseEntity<?> updateCliente(@PathVariable String folioCliente, @RequestBody Cliente clienteDetails) {
        try {
            Cliente clienteActualizado = clienteService.updateCliente(folioCliente, clienteDetails);
            return ResponseEntity.ok(clienteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{folioCliente}")
    public ResponseEntity<?> deleteCliente(@PathVariable String folioCliente) {
        try {
            clienteService.deleteCliente(folioCliente);
            return ResponseEntity.ok().body("Cliente eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioCliente}/baja")
    public ResponseEntity<?> darDeBajaCliente(@PathVariable String folioCliente) {
        try {
            clienteService.darDeBajaCliente(folioCliente);
            return ResponseEntity.ok().body("Cliente dado de baja correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{folioCliente}/activar")
    public ResponseEntity<?> activarCliente(@PathVariable String folioCliente) {
        try {
            clienteService.activarCliente(folioCliente);
            return ResponseEntity.ok().body("Cliente activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Cliente> searchClientes(@RequestParam String searchTerm) {
        return clienteService.searchClientes(searchTerm);
    }

    @GetMapping("/estatus/{estatus}")
    public List<Cliente> getClientesByEstatus(@PathVariable String estatus) {
        return clienteService.getClientesByEstatus(estatus);
    }
}