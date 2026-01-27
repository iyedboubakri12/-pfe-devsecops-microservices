package com.microservices.users.services;

import com.microservices.commonstudent.models.entity.Student;
import com.microservices.users.clients.CourseFeignClient;
import com.microservices.users.config.TestContainersConfig;
import com.microservices.users.models.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestContainersConfig.class)
@ActiveProfiles("test-integration")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @MockBean
    private CourseFeignClient courseFeignClient;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void shouldFindByNameAndLastNameWithPageable() {
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);

        Page<Student> result = studentService.findByNameAndLastNameWithPageable("John", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John");
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("DoeSmith");
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindByNameAndLastName() {
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);

        List<Student> result = studentService.findByNameAndLastName("John");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("DoeSmith");
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindAllById() {
        Student student1 = new Student();
        student1.setName("John");
        student1.setLastName("DoeSmith");
        student1.setEmail("john.doe@example.com");
        Student student2 = new Student();
        student2.setName("Jane");
        student2.setLastName("SmithJane");
        student2.setEmail("jane.smith@example.com");
        studentRepository.saveAll(List.of(student1, student2));

        Iterable<Student> result = studentService.findAllById(List.of(student1.getId(), student2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void shouldDeleteById() {
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");
        studentRepository.save(student);

        doNothing().when(courseFeignClient).deleteCourseByStudentId(anyLong());

        studentService.deleteById(student.getId());

        boolean exists = studentRepository.existsById(student.getId());
        assertThat(exists).isFalse();
        verify(courseFeignClient).deleteCourseByStudentId(student.getId());
    }

    @Test
    void shouldSaveStudent() {
        Student student = new Student();
        student.setName("John");
        student.setLastName("DoeSmith");
        student.setEmail("john.doe@example.com");

        Student savedStudent = studentService.save(student);

        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getName()).isEqualTo("John");
        assertThat(savedStudent.getLastName()).isEqualTo("DoeSmith");
        assertThat(studentRepository.existsById(savedStudent.getId())).isTrue();
    }
}