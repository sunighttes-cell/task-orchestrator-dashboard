package com.taskOrchestrator.app;

import com.taskOrchestrator.app.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

//turns Spring into a lightweight orchestration runtime
@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class TaskOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskOrchestratorApplication.class, args);
	}

}
