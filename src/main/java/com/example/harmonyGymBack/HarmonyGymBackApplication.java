package com.example.harmonyGymBack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.harmonyGymBack")
@EnableScheduling
public class HarmonyGymBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HarmonyGymBackApplication.class, args);
	}
}
