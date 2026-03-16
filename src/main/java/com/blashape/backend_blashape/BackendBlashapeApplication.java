package com.blashape.backend_blashape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BackendBlashapeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendBlashapeApplication.class, args);
	}

}
