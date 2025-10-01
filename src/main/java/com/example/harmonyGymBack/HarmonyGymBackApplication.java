package com.example.harmonyGymBack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.harmonyGymBack")
@EntityScan("model") // ← Escanear entidades en package "model"
@EnableJpaRepositories("repository") // ← Escanear repositories en package "repository"
public class HarmonyGymBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HarmonyGymBackApplication.class, args);
	}
}