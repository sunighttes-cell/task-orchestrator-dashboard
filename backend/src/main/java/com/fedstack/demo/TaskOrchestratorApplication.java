package com.fedstack.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//turns Spring into a lightweight orchestration runtime
@EnableScheduling
@SpringBootApplication
public class TaskOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskOrchestratorApplication.class, args);
	}

}
