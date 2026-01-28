package com.microservices.examenservice.models.repository;

import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonexam.models.entity.Question;
import com.microservices.examenservice.config.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ExamRepositoryIntegrationTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        examRepository.deleteAll();
    }

    @Test
    void shouldSaveExam() {
        Exam exam = new Exam();
        exam.setName("Math Exam");

        Exam savedExam = examRepository.save(exam);

        assertThat(savedExam).isNotNull();
        assertThat(savedExam.getId()).isNotNull();
        assertThat(savedExam.getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindByName() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        List<Exam> exams = examRepository.findByName("Math");

        assertThat(exams).hasSize(1);
        assertThat(exams.get(0).getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindByNameWithPageable() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        Page<Exam> result = examRepository.findByNameWithPageable("math", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindExamsIdWithAnswersByQuestionIds() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        Question question = new Question();
        question.setText("What is 2+2?");
        question.setExam(exam);
        exam.setQuestions(Arrays.asList(question));
        examRepository.save(exam);

        Iterable<Long> examIds = examRepository.findExamsIdWithAnswersByQuestionIds(Arrays.asList(question.getId()));

        assertThat(examIds).containsExactly(exam.getId());
    }
}