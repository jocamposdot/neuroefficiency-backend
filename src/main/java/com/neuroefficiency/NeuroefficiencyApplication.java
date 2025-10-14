package com.neuroefficiency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicação principal do Neuroefficiency
 * 
 * @author Neuroefficiency Team
 * @version 2.0 - Adicionado @EnableScheduling para jobs de limpeza
 * @since 2025-10-14
 */
@SpringBootApplication
@EnableScheduling  // Habilita jobs agendados (ex: limpeza de tokens expirados)
public class NeuroefficiencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeuroefficiencyApplication.class, args);
	}

}
