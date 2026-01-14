package com.microservices.users.models.repository;

import com.microservices.commonstudent.models.entity.Student;
import com.microservices.users.config.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test-integration")
public class StudentRepositoryIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void shouldFindByNameAndLastName() {
        // Créer un étudiant
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);

        // Exécuter la requête
        List<Student> result = studentRepository.findByNameAndLastName("John");

        // Vérifications
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("DoeSmith");
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindByNameAndLastNameWithPageable() {
        // Créer un étudiant
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);

        // Exécuter la requête
        Page<Student> result = studentRepository.findByNameAndLastNameWithPageable("John", PageRequest.of(0, 10));

        // Vérifications
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John");
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("DoeSmith");
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindAllById() {
        // Créer deux étudiants
        Student student1 = new Student();
        student1.setName("John");
        student1.setLastName("DoeSmith");
        student1.setEmail("john.doe@example.com");
        Student student2 = new Student();
        student2.setName("Jane");
        student2.setLastName("SmithJane");
        student2.setEmail("jane.smith@example.com");
        studentRepository.saveAll(List.of(student1, student2));

        // Exécuter la requête
        Iterable<Student> result = studentRepository.findAllById(List.of(student1.getId(), student2.getId()));

        // Vérifications
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void shouldSaveAndDeleteStudent() {
        // Créer un étudiant
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        Student savedStudent = studentRepository.save(student);

        // Vérifier la sauvegarde
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(studentRepository.existsById(savedStudent.getId())).isTrue();

        // Supprimer l'étudiant
        studentRepository.deleteById(savedStudent.getId());

        // Vérifier la suppression
        assertThat(studentRepository.existsById(savedStudent.getId())).isFalse();
    }
}