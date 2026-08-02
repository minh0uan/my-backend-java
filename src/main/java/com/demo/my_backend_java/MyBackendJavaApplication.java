package com.demo.my_backend_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MyBackendJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyBackendJavaApplication.class, args);
	}

}
