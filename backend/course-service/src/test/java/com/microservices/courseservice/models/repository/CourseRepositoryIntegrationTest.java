package com.microservices.courseservice.models.repository;

import com.microservices.courseservice.CourseServiceApplication;
import com.microservices.courseservice.config.TestContainersConfig;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.entity.CourseStudent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CourseServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-integration")
@Testcontainers
@Import(TestContainersConfig.class)
@Transactional
public class CourseRepositoryIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
    }

    @Test
    void shouldSaveCourse() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");

        Course savedCourse = courseRepository.save(course);

        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getName()).isEqualTo("Math");
        assertThat(savedCourse.getDescription()).isEqualTo("Mathematics Course");
    }

    @Test
    void shouldFindAllCourses() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        courseRepository.save(course);

        Iterable<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);
        assertThat(courses.iterator().next().getName()).isEqualTo("Math");
    }

    @Test
    void shouldFindCourseByStudentId() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        CourseStudent courseStudent = new CourseStudent();
        courseStudent.setStudentId(1L);
        courseStudent.setCourse(course);
        course.addCourseStudent(courseStudent);
        courseRepository.save(course);

        Course foundCourse = courseRepository.findCourseByStudentId(1L);

        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.getName()).isEqualTo("Math");
        assertThat(foundCourse.getCourseStudents()).hasSize(1);
        assertThat(foundCourse.getCourseStudents().get(0).getStudentId()).isEqualTo(1L);
    }

    @Test
    void shouldFindByNameOrDescriptionWithPageable() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        courseRepository.save(course);

        Page<Course> result = courseRepository.findByNameOrDescriptionWithPageable("math", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Math");
    }

    @Test
    void shouldDeleteCourseStudentById() {
        Course course = new Course();
        course.setName("Math");
        course.setDescription("Mathematics Course");
        CourseStudent courseStudent = new CourseStudent();
        courseStudent.setStudentId(1L);
        courseStudent.setCourse(course);
        course.addCourseStudent(courseStudent);
        Course savedCourse = courseRepository.save(course);

        courseRepository.deleteCourseStudentById(1L);
        courseRepository.flush();
        entityManager.clear();

        Course updatedCourse = courseRepository.findById(savedCourse.getId()).orElse(null);
        assertThat(updatedCourse).isNotNull();
        assertThat(updatedCourse.getCourseStudents()).isEmpty();
    }
}