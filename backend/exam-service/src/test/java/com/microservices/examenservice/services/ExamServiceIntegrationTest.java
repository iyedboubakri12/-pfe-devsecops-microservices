package com.microservices.examenservice.services;

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
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = ExamServiceApplication.class)
@Import(TestContainersConfig.class)
@ActiveProfiles("test-integration")
@EnableJpaRepositories(basePackages = "com.microservices.examenservice.models.repository")
@Transactional
public class ExamServiceIntegrationTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        examRepository.deleteAll();
        subjectRepository.deleteAll();
    }

    @Test
    void shouldSaveExam() {
        Exam exam = new Exam();
        exam.setName("Math Exam");

        Exam savedExam = examService.save(exam);

        assertThat(savedExam).isNotNull();
        assertThat(savedExam.getId()).isNotNull();
        assertThat(savedExam.getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindExamById() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        Exam savedExam = examRepository.save(exam);

        Exam foundExam = examService.findById(savedExam.getId());

        assertThat(foundExam).isNotNull();
        assertThat(foundExam.getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindExamsByName() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        List<Exam> exams = examService.findByName("Math");

        assertThat(exams).hasSize(1);
        assertThat(exams.get(0).getName()).isEqualTo("Math Exam");
    }

    @Test
    void shouldFindAllSubjects() {
        Subject subject = new Subject();
        subject.setName("Math");
        subjectRepository.save(subject);

        List<Subject> subjects = examService.findAllSubjects();

        assertThat(subjects).hasSize(1);
        assertThat(subjects.get(0).getName()).isEqualTo("Math");
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

        Iterable<Long> examIds = examService.findExamsIdWithAnswersByQuestionIds(Arrays.asList(question.getId()));

        assertThat(examIds).containsExactly(exam.getId());
    }

    @Test
    void shouldFindByNameWithPageable() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        examRepository.save(exam);

        Page<Exam> result = examService.findByNameWithPageable("Math", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Math Exam");
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void shouldDeleteExam() {
        Exam exam = new Exam();
        exam.setName("Math Exam");
        Exam savedExam = examRepository.save(exam);
        entityManager.flush();

        System.out.println("Saved Exam ID: " + savedExam.getId());
        boolean existsBefore = examRepository.existsById(savedExam.getId());
        System.out.println("Exists before delete: " + existsBefore);
        assertThat(existsBefore).isTrue();

        examRepository.deleteById(savedExam.getId());
        entityManager.flush(); // Forcer la synchronisation avec la base de données

        boolean existsAfter = examRepository.existsById(savedExam.getId());
        System.out.println("Exists after delete: " + existsAfter);
        assertThat(existsAfter).isFalse();
    }
}