package com.example.harmonyGymBack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DataMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void run(String... args) {
        migrarMembresiasConIdVacio();
    }

    public void migrarMembresiasConIdVacio() {
        try {
            // Verificar si existe la tabla membresia
            List<String> vacios = jdbcTemplate.query(
                    "SELECT id_membresia FROM membresia WHERE id_membresia IS NULL OR TRIM(id_membresia) = ''",
                    (rs, rowNum) -> rs.getString("id_membresia")
            );

            if (vacios != null && !vacios.isEmpty()) {
                log.info("Se encontraron {} membresías con id vacío. Iniciando migración...", vacios.size());

                for (int i = 0; i < vacios.size(); i++) {
                    // Calcular el siguiente folio MEMxxx
                    String maxMem = jdbcTemplate.queryForObject(
                            "SELECT COALESCE(MAX(id_membresia), 'MEM000') FROM membresia WHERE id_membresia LIKE 'MEM%' AND LENGTH(id_membresia) = 6",
                            String.class
                    );
                    int nextNum = 1;
                    if (maxMem != null && maxMem.startsWith("MEM")) {
                        try {
                            nextNum = Integer.parseInt(maxMem.substring(3).trim()) + 1;
                        } catch (NumberFormatException ignored) {}
                    }
                    String nuevoId = String.format("MEM%03d", nextNum);

                    // Actualizar una fila con id vacío
                    jdbcTemplate.update(
                            "UPDATE cliente_membresia SET id_membresia = ? WHERE id_membresia IS NULL OR TRIM(id_membresia) = ''",
                            nuevoId
                    );

                    try {
                        jdbcTemplate.update(
                                "UPDATE vende SET id_membresia = ? WHERE id_membresia IS NULL OR TRIM(id_membresia) = ''",
                                nuevoId
                        );
                    } catch (Exception e) {
                        log.debug("Columna id_membresia en vende no encontrada o ya actualizada: {}", e.getMessage());
                    }

                    jdbcTemplate.update(
                            "UPDATE membresia SET id_membresia = ? WHERE ctid IN (SELECT ctid FROM membresia WHERE id_membresia IS NULL OR TRIM(id_membresia) = '' LIMIT 1)",
                            nuevoId
                    );

                    log.info("Membresía vacía migrada a folio: {}", nuevoId);
                }
            }
        } catch (Exception e) {
            log.warn("Nota durante migración de membresías vacías: {}", e.getMessage());
        }
    }
}
