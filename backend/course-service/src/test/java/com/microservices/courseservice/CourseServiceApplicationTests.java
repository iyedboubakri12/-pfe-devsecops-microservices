package com.microservices.courseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test-unit")
class CourseServiceApplicationTests {

	@Test
	void contextLoads() {
		// Vérifie simplement que le contexte charge avec H2
	}
}