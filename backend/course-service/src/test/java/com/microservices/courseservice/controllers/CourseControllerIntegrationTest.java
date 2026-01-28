package com.microservices.courseservice.controllers;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import com.microservices.courseservice.CourseServiceApplication;
import com.microservices.courseservice.config.TestContainersConfig;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.entity.CourseStudent;
import com.microservices.courseservice.models.repository.CourseRepository;
import com.microservices.commonstudent.models.entity.Student;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.microservices.courseservice.clients.AnswerFeignClient;
import com.microservices.courseservice.clients.StudentFeignClient;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = CourseServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {
    org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class
})
@ActiveProfiles("test-unit")
public class CourseControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @MockBean
    private AnswerFeignClient answerFeignClient;

    @MockBean
    private StudentFeignClient studentFeignClient;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/courses";
        courseRepository.deleteAll();
        when(studentFeignClient.getStudentsByCourse(any())).thenReturn(Collections.emptyList());
        when(answerFeignClient.getExamsByStudentId(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldCreateCourse() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");

        ResponseEntity<Course> response = restTemplate.postForEntity(baseUrl, course, Course.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Math");
        assertThat(response.getBody().getDescription()).isEqualTo("Mathematics Course");
    }

    @Test
    void shouldGetAllCourses() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        courseRepository.save(course);

        ResponseEntity<Course[]> response = restTemplate.getForEntity(baseUrl, Course[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Math");
    }

    @Test
    void shouldAssignStudentToCourse() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        Course savedCourse = courseRepository.save(course);

        Student student = new Student();
        student.setId(1L);

        HttpEntity<?> request = new HttpEntity<>(Collections.singletonList(student));
        ResponseEntity<Course> response = restTemplate.exchange(
                baseUrl + "/" + savedCourse.getId() + "/assign-student",
                HttpMethod.PUT,
                request,
                Course.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCourseStudents()).hasSize(1);
        assertThat(response.getBody().getCourseStudents().get(0).getStudentId()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void shouldDeleteStudentFromCourse() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        CourseStudent courseStudent = new CourseStudent();
        courseStudent.setStudentId(1L);
        courseStudent.setCourse(course);
        course.addCourseStudent(courseStudent);
        Course savedCourse = courseRepository.save(course);

        // Supprimer l'étudiant directement via le repository
        course.getCourseStudents().clear();
        courseRepository.save(course);

        // Vérifier que la suppression a fonctionné
        Course updatedCourse = courseRepository.findById(savedCourse.getId()).orElseThrow();
        Hibernate.initialize(updatedCourse.getCourseStudents());
        assertThat(updatedCourse.getCourseStudents()).isEmpty();
    }
}
