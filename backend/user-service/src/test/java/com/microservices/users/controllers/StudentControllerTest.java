package com.microservices.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.users.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class StudentControllerTest {

    private MockMvc mockMvc; // Remove @Autowired

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Student sampleStudent;

    @BeforeEach
    public void setUp() {
        // Manually configure MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        sampleStudent = new Student();
        sampleStudent.setId(1L);
        sampleStudent.setName("John");
        sampleStudent.setLastName("Doesson");
        sampleStudent.setEmail("john.doesson@example.com");
        sampleStudent.setImage(new byte[]{1, 2, 3});
    }

    @Test
    public void testGetStudentsByCourse_Success() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(studentService.findAllById(ids)).thenReturn(Collections.singletonList(sampleStudent));

        mockMvc.perform(get("/students/students-by-course")
                        .param("ids", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doesson"))
                .andExpect(jsonPath("$[0].email").value("john.doesson@example.com"));
    }

    @Test
    public void testIndex_Success() throws Exception {
        Page<Student> page = new PageImpl<>(Collections.singletonList(sampleStudent));
        when(studentService.findAllPage(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/students/page/0/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doesson"));
    }

    @Test
    public void testIndexPageWithText_Success() throws Exception {
        Page<Student> page = new PageImpl<>(Collections.singletonList(sampleStudent));
        when(studentService.findByNameAndLastNameWithPageable(any(String.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/students/page/0/10/john")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doesson"));
    }

    @Test
    public void testUpdate_Success() throws Exception {
        Student updatedStudent = new Student();
        updatedStudent.setName("Jane");
        updatedStudent.setLastName("Smithson");
        updatedStudent.setEmail("jane.smithson@example.com");

        when(studentService.findById(1L)).thenReturn(sampleStudent);
        when(studentService.update(any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/students/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smithson"));
    }

    @Test
    public void testUpdate_ValidationError() throws Exception {
        Student invalidStudent = new Student();
        invalidStudent.setName("");
        invalidStudent.setLastName("Smithson");

        mockMvc.perform(put("/students/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFilter_Success() throws Exception {
        when(studentService.findByNameAndLastName(any(String.class)))
                .thenReturn(Collections.singletonList(sampleStudent));

        mockMvc.perform(get("/students/filter/john")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doesson"));
    }

    @Test
    public void testCreateWithImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        when(studentService.save(any(Student.class))).thenReturn(sampleStudent);

        mockMvc.perform(multipart("/students/create-with-image")
                        .file(file)
                        .param("name", "John")
                        .param("lastName", "Doesson")
                        .param("email", "john.doesson@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doesson"));
    }

    @Test
    public void testCreateWithImage_ValidationError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/students/create-with-image")
                        .file(file)
                        .param("name", "")
                        .param("lastName", "Doesson")
                        .param("email", "john.doesson@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        when(studentService.findById(1L)).thenReturn(sampleStudent);
        when(studentService.update(any(Student.class))).thenReturn(sampleStudent);

        mockMvc.perform(multipart("/students/1/update-with-image")
                        .file(file)
                        .param("name", "John")
                        .param("lastName", "Doesson")
                        .param("email", "john.doesson@example.com")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doesson"));
    }

    @Test
    public void testViewImage_Success() throws Exception {
        when(studentService.findById(1L)).thenReturn(sampleStudent);

        mockMvc.perform(get("/students/1/uploads/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(sampleStudent.getImage()));
    }

    @Test
    public void testViewImage_NotFound() throws Exception {
        Student studentNoImage = new Student();
        studentNoImage.setId(1L);
        studentNoImage.setName("John");
        studentNoImage.setLastName("Doesson");
        when(studentService.findById(1L)).thenReturn(studentNoImage);

        mockMvc.perform(get("/students/1/uploads/image"))
                .andExpect(status().isNotFound());
    }
}
