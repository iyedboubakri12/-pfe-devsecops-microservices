package com.microservices.answerservice.repository;

import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.models.repository.AnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.microservices.answerservice.config.TestContainersConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test") // Added to load application-test.properties
public class AnswerRepositoryIntegrationTest {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private MongoDBContainer mongoDBContainer;

    @BeforeEach
    void setUp() {
        answerRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindAnswerById() {
        Answer answer = new Answer();
        answer.setText("Sample answer");
        answer.setStudentId(1L);
        answer.setQuestionId(1L);
        answer.setExamId(1L);

        Answer savedAnswer = answerRepository.save(answer);

        Answer foundAnswer = answerRepository.findById(savedAnswer.getId()).orElse(null);

        assertThat(foundAnswer).isNotNull();
        assertThat(foundAnswer.getText()).isEqualTo("Sample answer");
        assertThat(foundAnswer.getStudentId()).isEqualTo(1L);
        assertThat(foundAnswer.getQuestionId()).isEqualTo(1L);
        assertThat(foundAnswer.getExamId()).isEqualTo(1L);
    }

    @Test
    void shouldFindAnswersByStudentIdAndExamId() {
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

        answerRepository.saveAll(List.of(answer1, answer2));

        Iterable<Answer> answers = answerRepository.findAnswerByStudentByExam(1L, 1L);

        assertThat(answers).hasSize(2);
        assertThat(answers).extracting(Answer::getText).containsExactlyInAnyOrder("Answer 1", "Answer 2");
    }
}