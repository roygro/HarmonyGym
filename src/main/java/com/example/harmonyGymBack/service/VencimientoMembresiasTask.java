package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.repository.MembresiaClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Order(2)
public class VencimientoMembresiasTask implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VencimientoMembresiasTask.class);

    @Autowired
    private MembresiaClienteRepository membresiaClienteRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        expirarMembresiasVencidas();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Mexico_City")
    @Transactional
    public void ejecutarProgramado() {
        expirarMembresiasVencidas();
    }

    @Transactional
    public int expirarMembresiasVencidas() {
        LocalDate hoy = LocalDate.now();
        int actualizadas = membresiaClienteRepository.actualizarMembresiasExpiradas(hoy);
        log.info("Vencimiento de membresías ejecutado para fecha {}: {} membresías marcadas como Expirada", hoy, actualizadas);
        return actualizadas;
    }
}
