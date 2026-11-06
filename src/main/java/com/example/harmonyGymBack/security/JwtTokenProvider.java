package com.example.harmonyGymBack.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final long expiracionMs;
    private final SecretKey key;

    public JwtTokenProvider(
            @Value("${jwt.secret:${JWT_SECRET:HarmonyGymSecretKey2026SuperSecureAndLongEnoughForHmacSha256Signature!}}") String secret,
            @Value("${jwt.expiracion-horas:${JWT_EXPIRACION_HORAS:8}}") int horasExpiracion) {
        this.expiracionMs = (long) horasExpiracion * 60 * 60 * 1000L;
        // Asegurar clave de al menos 256 bits (32 bytes)
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(secretBytes, 0, padded, 0, secretBytes.length);
            this.key = Keys.hmacShaKeyFor(padded);
        } else {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        }
    }

    public String generarToken(String username, String idUsuario, String tipoUsuario, String rol, String idPersona) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(username)
                .claim("idUsuario", idUsuario)
                .claim("tipoUsuario", tipoUsuario)
                .claim("rol", rol)
                .claim("idPersona", idPersona)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT no válido o expirado: {}", e.getMessage());
            return false;
        }
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String obtenerUsername(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerTipoUsuario(String token) {
        Object tipo = obtenerClaims(token).get("tipoUsuario");
        return tipo != null ? tipo.toString() : null;
    }

    public String obtenerRol(String token) {
        Object rol = obtenerClaims(token).get("rol");
        return rol != null ? rol.toString() : null;
    }

    public String obtenerIdPersona(String token) {
        Object id = obtenerClaims(token).get("idPersona");
        return id != null ? id.toString() : null;
    }
}
