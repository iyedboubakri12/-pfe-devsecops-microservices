package com.microservices.courseservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.entity.CourseStudent;
import com.microservices.courseservice.services.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.enabled=false"
})
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course sampleCourse;
    private Student sampleStudent;
    private Exam sampleExam;

    @BeforeEach
    public void setUp() {
        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setName("Test Course");
        sampleCourse.setDescription("Test Description");

        sampleStudent = new Student();
        sampleStudent.setId(1L);

        sampleExam = new Exam();
        sampleExam.setId(1L);
    }

    @Test
    public void testGetAll() throws Exception {
        CourseStudent cs = new CourseStudent();
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(Arrays.asList(cs));
        when(courseService.findAll()).thenReturn(Arrays.asList(sampleCourse));

        mockMvc.perform(get("/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Course"))
                .andExpect(jsonPath("$[0].students[0].id").value(1L));
        verify(courseService, times(1)).findAll();
    }

    @Test
    public void testGetAllPageableWithStudents() throws Exception {
        CourseStudent cs = new CourseStudent();
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(Arrays.asList(cs));
        Page<Course> page = new PageImpl<>(Arrays.asList(sampleCourse));
        when(courseService.findAllPage(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/courses/page/0/10/with-students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Course"))
                .andExpect(jsonPath("$.content[0].students[0].id").value(1L));
        verify(courseService, times(1)).findAllPage(any(PageRequest.class));
    }

    @Test
    public void testShow() throws Exception {
        CourseStudent cs = new CourseStudent();
        cs.setStudentId(1L);
        sampleCourse.setCourseStudents(Arrays.asList(cs));
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.getStudentsByCourse(anyList())).thenReturn(Arrays.asList(sampleStudent));

        mockMvc.perform(get("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Course"))
                .andExpect(jsonPath("$.students[0].id").value(1L));
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).getStudentsByCourse(anyList());
    }

    @Test
    public void testIndex() throws Exception {
        Page<Course> page = new PageImpl<>(Arrays.asList(sampleCourse));
        when(courseService.findAllPage(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/courses/page/0/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Course"));
        verify(courseService, times(1)).findAllPage(any(PageRequest.class));
    }

    @Test
    public void testIndexPageWithText() throws Exception {
        Page<Course> page = new PageImpl<>(Arrays.asList(sampleCourse));
        when(courseService.findByNameOrDescriptionWithPageable(anyString(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/courses/page/0/10/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Course"));
        verify(courseService, times(1)).findByNameOrDescriptionWithPageable(eq("test"), any(PageRequest.class));
    }

    @Test
    public void testUpdateCourse() throws Exception {
        Course updatedCourse = new Course();
        updatedCourse.setName("Updated Course");
        updatedCourse.setDescription("Updated Description");
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.update(any(Course.class))).thenReturn(updatedCourse);

        String json = objectMapper.writeValueAsString(updatedCourse);
        mockMvc.perform(put("/courses/1/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Course"));
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).update(any(Course.class));
    }

    @Test
    public void testAssignStudent() throws Exception {
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.save(any(Course.class))).thenReturn(sampleCourse);

        String json = objectMapper.writeValueAsString(Arrays.asList(sampleStudent));
        mockMvc.perform(put("/courses/1/assign-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Course"));
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).save(any(Course.class));
    }

    @Test
    public void testDeleteStudent() throws Exception {
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.save(any(Course.class))).thenReturn(sampleCourse);

        String json = objectMapper.writeValueAsString(Arrays.asList(sampleStudent));
        mockMvc.perform(delete("/courses/1/delete-student") // Changé de put() à delete()
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent()) // Changé de 201 à 204
                .andExpect(jsonPath("$.name").doesNotExist()); // Pas de contenu retourné
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).save(any(Course.class));
    }

    @Test
    public void testAssignExam() throws Exception {
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.save(any(Course.class))).thenReturn(sampleCourse);

        String json = objectMapper.writeValueAsString(Arrays.asList(sampleExam));
        mockMvc.perform(put("/courses/1/assign-exam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Course"));
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).save(any(Course.class));
    }

    @Test
    public void testDeleteExam() throws Exception {
        when(courseService.findById(1L)).thenReturn(sampleCourse);
        when(courseService.save(any(Course.class))).thenReturn(sampleCourse);

        String json = objectMapper.writeValueAsString(Arrays.asList(sampleExam));
        mockMvc.perform(put("/courses/1/delete-exam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Course"));
        verify(courseService, times(1)).findById(1L);
        verify(courseService, times(1)).save(any(Course.class));
    }

    @Test
    public void testSearchByStudentId() throws Exception {
        sampleCourse.setExams(Arrays.asList(sampleExam));
        when(courseService.findCourseByStudentId(1L)).thenReturn(sampleCourse);
        when(courseService.getExamsIdsWithAnswersByStudentId(1L)).thenReturn(Arrays.asList(1L));

        mockMvc.perform(get("/courses/student/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Course"))
                .andExpect(jsonPath("$.exams[0].replied").value(true));
        verify(courseService, times(1)).findCourseByStudentId(1L);
        verify(courseService, times(1)).getExamsIdsWithAnswersByStudentId(1L);
    }

    @Test
    public void testDeleteCourseByStudentId() throws Exception {
        doNothing().when(courseService).deleteCourseStudentById(1L);

        mockMvc.perform(delete("/courses/delete-student/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(courseService, times(1)).deleteCourseStudentById(1L);
    }
}
