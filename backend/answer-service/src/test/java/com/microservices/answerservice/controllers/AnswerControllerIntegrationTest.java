package com.microservices.answerservice.controllers;

import com.microservices.answerservice.AnswerServiceApplication;
import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.models.repository.AnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AnswerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-integration")
public class AnswerControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnswerRepository answerRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/answers";
        answerRepository.deleteAll();
    }

    @Test
    void shouldCreateAnswers() {
        Answer answer = new Answer();
        answer.setText("Sample answer");
        answer.setStudentId(1L);
        answer.setQuestionId(1L);
        answer.setExamId(1L);

        ResponseEntity<Answer[]> response = restTemplate.postForEntity(baseUrl, List.of(answer), Answer[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getText()).isEqualTo("Sample answer");
    }

    @Test
    void shouldGetAllAnswers() {
        Answer answer = new Answer();
        answer.setText("Sample answer");
        answer.setStudentId(1L);
        answer.setQuestionId(1L);
        answer.setExamId(1L);
        answerRepository.save(answer);

        ResponseEntity<Answer[]> response = restTemplate.getForEntity(baseUrl, Answer[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getText()).isEqualTo("Sample answer");
    }

    @Test
    void shouldUpdateAnswer() {
        Answer answer = new Answer();
        answer.setText("Old answer");
        answer.setStudentId(1L);
        answer.setQuestionId(1L);
        answer.setExamId(1L);
        Answer savedAnswer = answerRepository.save(answer);

        Answer updatedAnswer = new Answer();
        updatedAnswer.setText("Updated answer");
        updatedAnswer.setStudentId(1L);
        updatedAnswer.setQuestionId(1L);
        updatedAnswer.setExamId(1L);

        ResponseEntity<Answer> response = restTemplate.exchange(
                baseUrl + "/" + savedAnswer.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updatedAnswer),
                Answer.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getText()).isEqualTo("Updated answer");
    }

    @Test
    void shouldDeleteAnswer() {
        Answer answer = new Answer();
        answer.setText("Sample answer");
        answer.setStudentId(1L);
        answer.setQuestionId(1L);
        answer.setExamId(1L);
        Answer savedAnswer = answerRepository.save(answer);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedAnswer.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(answerRepository.findById(savedAnswer.getId())).isEmpty();
    }
}