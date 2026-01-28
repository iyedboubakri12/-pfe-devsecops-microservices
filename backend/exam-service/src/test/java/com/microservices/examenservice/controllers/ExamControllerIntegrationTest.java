package com.microservices.examenservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonexam.models.entity.Question;
import com.microservices.commonexam.models.entity.Subject;
import com.microservices.examenservice.ExamServiceApplication;
import com.microservices.examenservice.config.TestContainersConfig;
import com.microservices.examenservice.models.repository.ExamRepository;
import com.microservices.examenservice.models.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ExamServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExamControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/exams";
        examRepository.deleteAll();
        subjectRepository.deleteAll();
    }

    @Test
    void shouldGetExamsAnsweredByQuestionIds() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        Question question = new Question();
        question.setText("What is 2+2?");
        exam.setQuestions(Arrays.asList(question));
        examRepository.save(exam);

        ResponseEntity<Long[]> response = restTemplate.getForEntity(
                baseUrl + "/answered-by-exam?questionIds=" + question.getId(),
                Long[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(exam.getId());
    }

    @Test
    void shouldGetExamsPage() {
        // Préparer les données
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        // Appeler l'API et récupérer la réponse comme String
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/page/0/10",
                HttpMethod.GET,
                null,
                String.class
        );

        // Vérifier le statut HTTP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Parser la réponse JSON pour vérifier le contenu
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Convertir la réponse JSON en un objet Map ou JsonNode
            var jsonNode = mapper.readTree(response.getBody());
            // Vérifier que la clé "content" contient un tableau avec 1 élément
            assertThat(jsonNode.get("content").isArray()).isTrue();
            assertThat(jsonNode.get("content").size()).isEqualTo(1);
            // Vérifier que le premier élément a le nom "Math Exam"
            assertThat(jsonNode.get("content").get(0).get("name").asText()).isEqualTo("Math Exam");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du parsing JSON", e);
        }
    }

    @Test
    void shouldGetExamsPageWithText() {
        // Préparer les données
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        // Appeler l'API et récupérer la réponse comme String
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/page/0/10/Math",
                HttpMethod.GET,
                null,
                String.class
        );

        // Vérifier le statut HTTP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Parser la réponse JSON pour vérifier le contenu
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Convertir la réponse JSON en un objet JsonNode
            var jsonNode = mapper.readTree(response.getBody());
            // Vérifier que la clé "content" contient un tableau avec 1 élément
            assertThat(jsonNode.get("content").isArray()).isTrue();
            assertThat(jsonNode.get("content").size()).isEqualTo(1);
            // Vérifier que le premier élément a le nom "Math Exam"
            assertThat(jsonNode.get("content").get(0).get("name").asText()).isEqualTo("Math Exam");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du parsing JSON", e);
        }
    }
    @Test
    void shouldEditExam() {
        Subject subject = new Subject();
        subject.setName("Math");
        subjectRepository.save(subject);

        Exam exam = new Exam();
        exam.setName("Math Exam");
        exam.setSubjectFather(subject);
        Exam savedExam = examRepository.save(exam);

        Exam updatedExam = new Exam();
        updatedExam.setName("Updated Math Exam");
        Question question = new Question();
        question.setText("What is 3+3?");
        updatedExam.setQuestions(Collections.singletonList(question));
        updatedExam.setSubjectFather(subject);

        HttpEntity<Exam> request = new HttpEntity<>(updatedExam);
        ResponseEntity<Exam> response = restTemplate.exchange(
                baseUrl + "/" + savedExam.getId() + "/exam",
                HttpMethod.PUT,
                request,
                Exam.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Updated Math Exam");
        assertThat(response.getBody().getQuestions()).hasSize(1);
        assertThat(response.getBody().getQuestions().get(0).getText()).isEqualTo("What is 3+3?");
    }

    @Test
    void shouldFilterExamsByName() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        ResponseEntity<Exam[]> response = restTemplate.getForEntity(
                baseUrl + "/filter/Math",
                Exam[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldGetAllSubjects() {
        Subject subject = new Subject();
        subject.setName("Math");
        subjectRepository.save(subject);

        ResponseEntity<Subject[]> response = restTemplate.getForEntity(
                baseUrl + "/subjects",
                Subject[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Math");
    }
}