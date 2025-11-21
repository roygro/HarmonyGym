package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.service.ClienteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteServiceImpl clienteService;

    // ==================== CREAR CLIENTE CON FOTO ====================

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> crearClienteConFoto(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "fechaNacimiento", required = false) String fechaNacimiento,
            @RequestParam(value = "genero", required = false) String genero,
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        try {
            Cliente cliente = clienteService.crearClienteConFoto(
                    nombre, telefono, email, fechaNacimiento, genero, estatus, foto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente creado exitosamente");
            response.put("cliente", cliente);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR URL DE FOTO ====================

    @PutMapping("/{folioCliente}/url-foto")
    public ResponseEntity<?> actualizarUrlFoto(
            @PathVariable String folioCliente,
            @RequestParam("urlFoto") String urlFoto) {

        try {
            Cliente clienteActualizado = clienteService.actualizarUrlFoto(folioCliente, urlFoto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "URL de foto actualizada exitosamente");
            response.put("cliente", clienteActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar URL de foto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== OBTENER FOTO ====================

    @GetMapping("/{folioCliente}/foto")
    public ResponseEntity<byte[]> obtenerFotoCliente(@PathVariable String folioCliente) {
        try {
            byte[] foto = clienteService.obtenerFotoCliente(folioCliente);

            if (foto == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(foto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS EXISTENTES ====================

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodosLosClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{folioCliente}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable String folioCliente) {
        try {
            Cliente cliente = clienteService.obtenerClientePorId(folioCliente);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filtros")
    public ResponseEntity<List<Cliente>> obtenerClientesFiltrados(
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "genero", required = false) String genero) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesFiltrados(estatus, genero);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{folioCliente}/estatus")
    public ResponseEntity<?> cambiarEstatusCliente(
            @PathVariable String folioCliente,
            @RequestParam String estatus) {
        try {
            Cliente cliente = clienteService.cambiarEstatusCliente(folioCliente, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estatus actualizado exitosamente");
            response.put("cliente", cliente);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{folioCliente}")
    public ResponseEntity<?> eliminarCliente(@PathVariable String folioCliente) {
        try {
            clienteService.eliminarCliente(folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente desactivado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{folioCliente}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasCliente(@PathVariable String folioCliente) {
        try {
            Map<String, Object> estadisticas = clienteService.obtenerEstadisticasCliente(folioCliente);
            return ResponseEntity.ok(estadisticas);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Cliente>> buscarClientesPorNombre(@RequestParam String nombre) {
        try {
            List<Cliente> clientes = clienteService.buscarClientesPorNombre(nombre);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Cliente>> obtenerClientesActivos() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesActivos();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/con-membresia-activa")
    public ResponseEntity<List<Cliente>> obtenerClientesConMembresiaActiva() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesConMembresiaActiva();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estadisticas-generales")
    public ResponseEntity<?> obtenerEstadisticasGenerales() {
        try {
            Map<String, Object> estadisticas = clienteService.obtenerEstadisticasGenerales();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ACTUALIZAR SOLO LA FOTO (URL) ====================

    @PutMapping("/{folioCliente}/foto-url")
    public ResponseEntity<?> actualizarFotoUrl(
            @PathVariable String folioCliente,
            @RequestParam("nombreArchivoFoto") String nombreArchivoFoto) {

        try {
            Cliente clienteExistente = clienteService.obtenerClientePorId(folioCliente);
            clienteExistente.setNombreArchivoFoto(nombreArchivoFoto);

            Cliente clienteActualizado = clienteService.actualizarCliente(folioCliente, clienteExistente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Foto actualizada exitosamente");
            response.put("cliente", clienteActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar foto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}