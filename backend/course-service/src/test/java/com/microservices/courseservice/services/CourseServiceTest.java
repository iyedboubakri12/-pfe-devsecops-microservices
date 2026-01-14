package com.microservices.courseservice.services;

import com.microservices.commonstudent.models.entity.Student;
import com.microservices.courseservice.clients.AnswerFeignClient;
import com.microservices.courseservice.clients.StudentFeignClient;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.entity.CourseStudent;
import com.microservices.courseservice.models.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test-unit")
@Transactional
public class CourseServiceTest {

    @Autowired
    private CourseRepository courseRepository;

    @MockBean
    private AnswerFeignClient answerFeignClient;

    @MockBean
    private StudentFeignClient studentFeignClient;

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private EntityManager entityManager; // Pour gérer la session

    private Course sampleCourse;
    private Student sampleStudent;

    @BeforeEach
    public void setUp() {
        sampleCourse = new Course();
        sampleCourse.setName("Test Course");
        sampleCourse.setDescription("Test Description");

        sampleStudent = new Student();
        sampleStudent.setId(1L);

        courseRepository.save(sampleCourse); // H2 génère l'ID automatiquement
    }

    @Test
    public void testFindAll() {
        Iterable<Course> courses = courseService.findAll();
        assertNotNull(courses);
        List<Course> courseList = (List<Course>) courses;
        assertTrue(courseList.stream().anyMatch(c -> c.getName().equals("Test Course")));
        assertEquals(1, courseList.size());
    }

    @Test
    public void testFindById() {
        Course savedCourse = courseRepository.findAll().iterator().next();
        Course course = courseService.findById(savedCourse.getId());
        assertNotNull(course);
        assertEquals("Test Course", course.getName());
    }

    @Test
    public void testSave() {
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setDescription("New Description");
        Course savedCourse = courseService.save(newCourse);
        assertNotNull(savedCourse);
        assertNotNull(savedCourse.getId());
        assertEquals("New Course", savedCourse.getName());
    }

    @Test
    public void testFindCourseByStudentId() {
        CourseStudent cs = new CourseStudent();
        cs.setCourse(sampleCourse);
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(new ArrayList<>(Arrays.asList(cs)));
        courseRepository.save(sampleCourse);

        Course course = courseService.findCourseByStudentId(1L);
        assertNotNull(course);
        assertEquals("Test Course", course.getName());
    }

    @Test
    public void testGetExamsIdsWithAnswersByStudentId() {
        List<Long> examIds = Arrays.asList(1L, 2L);
        when(answerFeignClient.getExamsByStudentId(1L)).thenReturn(examIds);

        Iterable<Long> result = courseService.getExamsIdsWithAnswersByStudentId(1L);
        assertNotNull(result);
        List<Long> resultList = (List<Long>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(1L));
        verify(answerFeignClient, times(1)).getExamsByStudentId(1L);
    }

    @Test
    public void testGetStudentsByCourse() {
        List<Long> ids = Arrays.asList(1L);
        when(studentFeignClient.getStudentsByCourse(ids)).thenReturn(Arrays.asList(sampleStudent));

        Iterable<Student> students = courseService.getStudentsByCourse(ids);
        assertNotNull(students);
        List<Student> studentList = (List<Student>) students;
        assertEquals(1, studentList.size());
        assertEquals(1L, studentList.get(0).getId());
        verify(studentFeignClient, times(1)).getStudentsByCourse(ids);
    }

    @Test
    public void testDeleteCourseStudentById() {
        // Arrange
        CourseStudent cs = new CourseStudent();
        cs.setCourse(sampleCourse);
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(new ArrayList<>(Arrays.asList(cs)));
        courseRepository.save(sampleCourse);

        Long courseId = sampleCourse.getId();

        // Act
        courseService.deleteCourseStudentById(1L);

        // Assert
        entityManager.clear(); // Vide le cache de la session pour forcer un rechargement depuis la base
        Course updatedCourse = courseRepository.findById(courseId).orElse(null);
        assertNotNull(updatedCourse);
        assertEquals(0, updatedCourse.getCourseStudents().size()); // Devrait être 0
    }

    @Test
    public void testFindByNameOrDescriptionWithPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> result = courseService.findByNameOrDescriptionWithPageable("Test", pageable);
        assertNotNull(result);
        List<Course> content = result.getContent();
        assertEquals(1, content.size());
        assertEquals("Test Course", content.get(0).getName());
    }

    @Test
    public void testFindAllPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> result = courseService.findAllPage(pageable);
        assertNotNull(result);
        List<Course> content = result.getContent();
        assertEquals(1, content.size());
        assertEquals("Test Course", content.get(0).getName());
    }
}