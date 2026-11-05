package com.example.harmonyGymBack.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extraerToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validarToken(token)) {
                Claims claims = jwtTokenProvider.obtenerClaims(token);
                String username = claims.getSubject();
                String tipoUsuario = claims.get("tipoUsuario", String.class);
                String rol = claims.get("rol", String.class);
                String idPersona = claims.get("idPersona", String.class);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(tipoUsuario)) {
                    // Formato: ROLE_ADMINISTRADOR, ROLE_RECEPCIONISTA, ROLE_INSTRUCTOR, ROLE_CLIENTE
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.toUpperCase()));
                }
                if (StringUtils.hasText(rol) && !rol.equalsIgnoreCase(tipoUsuario)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
                }

                // Creamos el principal que contiene username y detalles adicionales
                UsuarioPrincipal principal = new UsuarioPrincipal(username, tipoUsuario, rol, idPersona);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("No se pudo configurar la autenticación del usuario en el contexto de seguridad", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
