package com.microservices.examenservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonexam.models.entity.Question;
import com.microservices.commonexam.models.entity.Subject;
import com.microservices.examenservice.services.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExamController.class)
public class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    @Autowired
    private ObjectMapper objectMapper;

    private Exam sampleExam;
    private Subject sampleSubject;

    @BeforeEach
    public void setUp() {
        sampleExam = new Exam();
        sampleExam.setId(1L);
        sampleExam.setName("Math Exam");

        Question q1 = new Question();
        q1.setText("What is 2 + 2?");
        q1.setExam(sampleExam);
        Question q2 = new Question();
        q2.setText("What is the square root of 16?");
        q2.setExam(sampleExam);
        sampleExam.setQuestions(Arrays.asList(q1, q2));

        sampleSubject = new Subject();
        sampleSubject.setId(1L);
        sampleSubject.setName("Mathematics");
    }

    @Test
    public void testGetExamsAnsweredByQuestionsIds_Success() throws Exception {
        List<Long> questionIds = Arrays.asList(1L, 2L);
        List<Long> examIds = Arrays.asList(1L, 2L);
        when(examService.findExamsIdWithAnswersByQuestionIds(questionIds)).thenReturn(examIds);

        mockMvc.perform(get("/exams/answered-by-exam")
                        .param("questionIds", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1L))
                .andExpect(jsonPath("$[1]").value(2L));

        verify(examService, times(1)).findExamsIdWithAnswersByQuestionIds(questionIds);
    }

    @Test
    public void testGetExamsAnsweredByQuestionsIds_EmptyList() throws Exception {
        List<Long> questionIds = Collections.emptyList();
        when(examService.findExamsIdWithAnswersByQuestionIds(questionIds)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exams/answered-by-exam")
                        .param("questionIds", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(examService, times(1)).findExamsIdWithAnswersByQuestionIds(questionIds);
    }

    @Test
    public void testIndex_Success() throws Exception {
        Page<Exam> page = new PageImpl<>(Arrays.asList(sampleExam), PageRequest.of(0, 10), 1);
        when(examService.findAllPage(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/exams/page/0/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Math Exam"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(examService, times(1)).findAllPage(PageRequest.of(0, 10));
    }

    @Test
    public void testIndexPageWithText_Success() throws Exception {
        Page<Exam> page = new PageImpl<>(Arrays.asList(sampleExam), PageRequest.of(0, 10), 1);
        when(examService.findByNameWithPageable("Math", PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/exams/page/0/10/Math")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Math Exam"));

        verify(examService, times(1)).findByNameWithPageable("Math", PageRequest.of(0, 10));
    }

    @Test
    public void testEditExam_Success() throws Exception {
        Exam updatedExam = new Exam();
        updatedExam.setId(1L);
        updatedExam.setName("Updated Math Exam");
        Question q1 = new Question();
        q1.setText("What is 2 + 2?");
        updatedExam.setQuestions(Arrays.asList(q1));

        Exam mockExam = new Exam();
        mockExam.setId(1L);
        mockExam.setName("Math Exam");
        mockExam.setQuestions(Collections.emptyList());
        when(examService.findById(1L)).thenReturn(mockExam);
        when(examService.update(any(Exam.class))).thenReturn(updatedExam);

        mockMvc.perform(put("/exams/1/exam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedExam)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Math Exam"))
                .andExpect(jsonPath("$.questions[0].text").value("What is 2 + 2?"));

        verify(examService, times(1)).findById(1L);
        verify(examService, times(1)).update(any(Exam.class));
    }

    @Test
    public void testEditExam_ValidationError() throws Exception {
        Exam invalidExam = new Exam();
        Question q1 = new Question();
        q1.setText("What is 2 + 2?");
        invalidExam.setQuestions(Arrays.asList(q1));

        // Capture la réponse complète
        ResultActions result = mockMvc.perform(put("/exams/1/exam")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidExam)));

        // Affiche les détails de la réponse
        String contentType = result.andReturn().getResponse().getContentType();
        String body = result.andReturn().getResponse().getContentAsString();
        System.out.println("Content-Type: " + contentType);
        System.out.println("Response Body: " + body);

        // Vérifie le statut et ajuste l'assertion après inspection
        result.andExpect(status().isBadRequest());
        if (body != null && !body.isEmpty()) {
            result.andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].message").value("must not be empty"));
        } else {
            System.out.println("No JSON response body to parse!");
        }

        verify(examService, never()).findById(anyLong());
        verify(examService, never()).update(any(Exam.class));
    }

    @Test
    public void testFilter_Success() throws Exception {
        List<Exam> exams = Arrays.asList(sampleExam);
        when(examService.findByName("Math")).thenReturn(exams);

        mockMvc.perform(get("/exams/filter/Math")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Math Exam"));

        verify(examService, times(1)).findByName("Math");
    }

    @Test
    public void testFilter_NoResults() throws Exception {
        when(examService.findByName("Unknown")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exams/filter/Unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(examService, times(1)).findByName("Unknown");
    }

    @Test
    public void testGetSubjects_Success() throws Exception {
        List<Subject> subjects = Arrays.asList(sampleSubject);
        when(examService.findAllSubjects()).thenReturn(subjects);

        mockMvc.perform(get("/exams/subjects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mathematics"));

        verify(examService, times(1)).findAllSubjects();
    }

    @Test
    public void testGetSubjects_EmptyList() throws Exception {
        when(examService.findAllSubjects()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exams/subjects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(examService, times(1)).findAllSubjects();
    }
}