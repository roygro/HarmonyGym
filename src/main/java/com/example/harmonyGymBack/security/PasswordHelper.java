package com.example.harmonyGymBack.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordHelper {

    private static final String MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMEROS = "0123456789";
    private static final String SIMBOLOS = "!@#$%^&*()-_=+[]{}";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS + SIMBOLOS;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera una contraseña temporal segura con SecureRandom:
     * Mínimo 12 caracteres, con mayúsculas, minúsculas, números y símbolos.
     */
    public static String generarPasswordTemporalSegura() {
        List<Character> caracteres = new ArrayList<>();

        // Asegurar al menos uno de cada categoría requerida
        caracteres.add(MAYUSCULAS.charAt(RANDOM.nextInt(MAYUSCULAS.length())));
        caracteres.add(MINUSCULAS.charAt(RANDOM.nextInt(MINUSCULAS.length())));
        caracteres.add(NUMEROS.charAt(RANDOM.nextInt(NUMEROS.length())));
        caracteres.add(SIMBOLOS.charAt(RANDOM.nextInt(SIMBOLOS.length())));

        // Rellenar hasta 12 caracteres con la combinación completa
        for (int i = 4; i < 12; i++) {
            caracteres.add(TODOS.charAt(RANDOM.nextInt(TODOS.length())));
        }

        // Mezclar aleatoriamente el resultado
        Collections.shuffle(caracteres, RANDOM);

        StringBuilder sb = new StringBuilder(caracteres.size());
        for (Character c : caracteres) {
            sb.append(c);
        }
        return sb.toString();
    }
}
