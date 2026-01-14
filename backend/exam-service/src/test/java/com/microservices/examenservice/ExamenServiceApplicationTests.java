package com.microservices.examenservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class ExamenServiceApplicationTests {

	@Test
	void contextLoads() {
		// Vérifie simplement que le contexte charge avec H2
	}

}
