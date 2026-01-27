package com.microservices.courseservice.services;

import com.microservices.courseservice.config.TestContainersConfig;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.entity.CourseStudent;
import com.microservices.courseservice.models.repository.CourseRepository;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.courseservice.clients.AnswerFeignClient;
import com.microservices.courseservice.clients.StudentFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test-integration")
@Testcontainers
@Import(TestContainersConfig.class)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CourseServiceIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private AnswerFeignClient answerFeignClient;

    @MockBean
    private StudentFeignClient studentFeignClient;

    private Course sampleCourse;
    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS courses_exams (" +
                        "course_id BIGINT NOT NULL, " +
                        "exam_id BIGINT NOT NULL, " +
                        "PRIMARY KEY (course_id, exam_id), " +
                        "FOREIGN KEY (course_id) REFERENCES courses(id), " +
                        "FOREIGN KEY (exam_id) REFERENCES exams(id))"
        );
        courseRepository.deleteAll();
        sampleCourse = new Course();
        sampleCourse.setName("Test Course");
        sampleCourse.setDescription("Test Description");

        sampleStudent = new Student();
        sampleStudent.setId(1L);

        when(studentFeignClient.getStudentsByCourse(any())).thenReturn(Collections.emptyList());
        when(answerFeignClient.getExamsByStudentId(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldSaveCourse() {
        Course savedCourse = courseService.save(sampleCourse);
        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getName()).isEqualTo("Test Course");
        assertThat(savedCourse.getDescription()).isEqualTo("Test Description");
    }

    @Test
    void shouldFindAllCourses() {
        courseRepository.save(sampleCourse);
        Iterable<Course> courses = courseService.findAll();
        assertThat(courses).hasSize(1);
        assertThat(courses.iterator().next().getName()).isEqualTo("Test Course");
    }

    @Test
    void shouldFindCourseByStudentId() {
        CourseStudent cs = new CourseStudent();
        cs.setCourse(sampleCourse);
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(Arrays.asList(cs));
        courseRepository.save(sampleCourse);

        Course course = courseService.findCourseByStudentId(1L);
        assertThat(course).isNotNull();
        assertThat(course.getName()).isEqualTo("Test Course");
        assertThat(course.getCourseStudents()).hasSize(1);
        assertThat(course.getCourseStudents().get(0).getStudentId()).isEqualTo(1L);
    }

    @Test
    void shouldFindByNameOrDescriptionWithPageable() {
        courseRepository.save(sampleCourse);
        Page<Course> result = courseService.findByNameOrDescriptionWithPageable("test", PageRequest.of(0, 10));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Course");
    }

    @Test
    void shouldDeleteCourseStudentById() {
        CourseStudent cs = new CourseStudent();
        cs.setCourse(sampleCourse);
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(Arrays.asList(cs));
        Course savedCourse = courseRepository.save(sampleCourse);

        courseService.deleteCourseStudentById(1L);
        courseRepository.flush();
        entityManager.clear();

        Course updatedCourse = courseRepository.findById(savedCourse.getId()).orElse(null);
        assertThat(updatedCourse).isNotNull();
        assertThat(updatedCourse.getCourseStudents()).isEmpty();
    }
}