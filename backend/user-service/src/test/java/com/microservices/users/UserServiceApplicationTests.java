package com.microservices.users;

import com.microservices.users.clients.CourseFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Utilise le profil "test" pour charger les propriétés spécifiques aux tests
public class UserServiceApplicationTests {

	@MockBean
	private CourseFeignClient courseFeignClient; // Simule le client Feign pour éviter les appels réseau

	@Test
	void contextLoads() {
		// Vérifie que le contexte Spring se charge correctement
	}
}