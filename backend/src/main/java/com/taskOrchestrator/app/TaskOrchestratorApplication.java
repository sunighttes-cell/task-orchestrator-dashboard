package com.taskOrchestrator.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class TaskOrchestratorApplication {
	public static void main(String[] args) {
		SpringApplication.run(
				TaskOrchestratorApplication.class,
				args
		);
	}
}