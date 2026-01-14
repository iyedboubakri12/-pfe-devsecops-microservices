package com.microservices.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.users.clients.CourseFeignClient;
import com.microservices.users.config.TestContainersConfig;
import com.microservices.users.models.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-integration")
@Import(TestContainersConfig.class)
@Transactional
public class StudentControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(StudentControllerIntegrationTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseFeignClient courseFeignClient;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/students";
        // Nettoyer la base de données
        studentRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void shouldGetStudentsPage() throws Exception {
        // Insérer un étudiant
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);
        entityManager.flush();

        // Appeler l'API
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/page/0/10",
                HttpMethod.GET,
                null,
                String.class
        );

        // Vérifications
        log.info("Response JSON: {}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Parser le JSON
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        assertThat(responseMap).containsKey("content");
    }

    @Test
    void shouldGetStudentsPageWithText() throws Exception {
        // Insérer un étudiant
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);
        entityManager.flush();

        // Appeler l'API
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/page/0/10/John",
                HttpMethod.GET,
                null,
                String.class
        );

        // Vérifications
        log.info("Response JSON: {}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Parser le JSON
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        assertThat(responseMap).containsKey("content");
    }
}