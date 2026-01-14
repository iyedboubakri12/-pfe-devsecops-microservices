package com.microservices.answerservice.services;

import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.models.repository.AnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.microservices.answerservice.config.TestContainersConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test") // Added to load application-test.properties
public class AnswerServiceIntegrationTest {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerRepository answerRepository;

    @BeforeEach
    void setUp() {
        answerRepository.deleteAll();
    }

    @Test
    void shouldSaveMultipleAnswers() {
        Answer answer1 = new Answer();
        answer1.setText("Answer 1");
        answer1.setStudentId(1L);
        answer1.setQuestionId(1L);
        answer1.setExamId(1L);

        Answer answer2 = new Answer();
        answer2.setText("Answer 2");
        answer2.setStudentId(1L);
        answer2.setQuestionId(2L);
        answer2.setExamId(1L);

        List<Answer> answers = List.of(answer1, answer2);
        Iterable<Answer> savedAnswers = answerService.saveAll(answers);

        assertThat(savedAnswers).hasSize(2);
        assertThat(savedAnswers).extracting(Answer::getText).containsExactlyInAnyOrder("Answer 1", "Answer 2");
    }

    @Test
    void shouldFindExamsIdsByStudentId() {
        Answer answer1 = new Answer();
        answer1.setText("Answer 1");
        answer1.setStudentId(1L);
        answer1.setQuestionId(1L);
        answer1.setExamId(1L);

        Answer answer2 = new Answer();
        answer2.setText("Answer 2");
        answer2.setStudentId(1L);
        answer2.setQuestionId(2L);
        answer2.setExamId(2L);

        answerRepository.saveAll(List.of(answer1, answer2));

        Iterable<Long> examIds = answerService.findExamsIdByWithAnswersByStudent(1L);

        assertThat(examIds).containsExactlyInAnyOrder(1L, 2L);
    }
}