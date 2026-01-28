package com.microservices.answerservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.profiles.active=test-integration")
@ActiveProfiles("test-integration")
class AnswerServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
