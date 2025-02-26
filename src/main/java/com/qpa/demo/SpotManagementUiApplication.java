package com.qpa.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.qpa.controller")
public class SpotManagementUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotManagementUiApplication.class, args);
	}

}
