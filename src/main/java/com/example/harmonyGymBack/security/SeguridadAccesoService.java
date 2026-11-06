package com.example.harmonyGymBack.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("seguridadAcceso")
public class SeguridadAccesoService {

    public boolean puedeAccederCliente(String folioCliente, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Administrador, Recepcionista e Instructor pueden consultar información de clientes
        boolean esPersonal = authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") ||
                a.getAuthority().equals("ROLE_RECEPCIONISTA") ||
                a.getAuthority().equals("ROLE_INSTRUCTOR")
        );

        if (esPersonal) {
            return true;
        }

        // Si es rol CLIENTE, solo puede ver sus propios datos
        if (authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            return folioCliente != null && folioCliente.equalsIgnoreCase(principal.getIdPersona());
        }

        return false;
    }
}
