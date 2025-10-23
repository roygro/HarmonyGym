package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.AuthRequest;
import com.example.harmonyGymBack.model.AuthResponse;
import com.example.harmonyGymBack.model.RegisterRequest;
import com.example.harmonyGymBack.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ==================== LOGIN ====================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            System.out.println("🔐 Solicitud de login recibida para: " + authRequest.getUsername());

            AuthResponse response = authService.autenticarUsuario(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en endpoint de login: " + e.getMessage());
            AuthResponse errorResponse = new AuthResponse(false, "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== REGISTRO ====================

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("🚀 Solicitud de registro recibida para: " + registerRequest.getUsername());
            System.out.println("📋 Tipo de usuario: " + registerRequest.getTipoUsuario());
            System.out.println("👤 ID Persona: " + registerRequest.getIdPersona());

            // Validaciones básicas
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse(false, "El username es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse(false, "La contraseña es requerida");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (registerRequest.getTipoUsuario() == null || registerRequest.getTipoUsuario().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse(false, "El tipo de usuario es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (registerRequest.getIdPersona() == null || registerRequest.getIdPersona().trim().isEmpty()) {
                AuthResponse errorResponse = new AuthResponse(false, "El ID de persona es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            AuthResponse response = authService.registrarUsuario(registerRequest);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en endpoint de registro: " + e.getMessage());
            AuthResponse errorResponse = new AuthResponse(false, "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== CAMBIAR CONTRASEÑA ====================

    @PostMapping("/cambiar-password")
    public ResponseEntity<Map<String, Object>> cambiarPassword(
            @RequestParam String username,
            @RequestParam String nuevaPassword) {
        try {
            System.out.println("🔑 Solicitud de cambio de contraseña para: " + username);

            Map<String, Object> response = authService.cambiarPassword(username, nuevaPassword);

            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en cambio de contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error interno del servidor")
            );
        }
    }

    // ==================== VERIFICAR DISPONIBILIDAD DE USERNAME ====================

    @GetMapping("/verificar-username")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidadUsername(
            @RequestParam String username) {
        try {
            System.out.println("🔍 Verificando disponibilidad de username: " + username);

            Map<String, Object> response = authService.verificarDisponibilidadUsername(username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error verificando username: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("disponible", false, "message", "Error interno del servidor")
            );
        }
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Servicio de autenticación funcionando"));
    }
}