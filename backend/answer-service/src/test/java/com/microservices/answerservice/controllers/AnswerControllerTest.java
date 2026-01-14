package com.microservices.answerservice.controllers;

import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.services.AnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnswerController.class)
public class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simule les requêtes HTTP

    @MockBean
    private AnswerService answerService; // Simule le service

    private Answer sampleAnswer;

    @BeforeEach
    public void setUp() {
        // Prépare un exemple de réponse
        sampleAnswer = new Answer();
        sampleAnswer.setId("1");
        sampleAnswer.setText("Test Answer");
        sampleAnswer.setStudentId(1L);
        sampleAnswer.setQuestionId(1L);
        sampleAnswer.setExamId(1L);
    }

    @Test
    public void testGetAll() throws Exception {
        when(answerService.findAll()).thenReturn(Arrays.asList(sampleAnswer));
        mockMvc.perform(get("/answers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].text").value("Test Answer"));
        verify(answerService, times(1)).findAll();
    }

    @Test
    public void testCreate() throws Exception {
        when(answerService.saveAll(anyIterable())).thenReturn(Arrays.asList(sampleAnswer));
        String json = "[{\"text\": \"Test Answer\", \"studentId\": 1, \"questionId\": 1, \"examId\": 1}]";
        mockMvc.perform(post("/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("1"));
        verify(answerService, times(1)).saveAll(anyIterable());
    }

    @Test
    public void testGetAnswersByStudentAndExam() throws Exception {
        when(answerService.findAnswerByStudentByExam(1L, 1L)).thenReturn(Arrays.asList(sampleAnswer));
        mockMvc.perform(get("/answers/student/1/exam/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
        verify(answerService, times(1)).findAnswerByStudentByExam(1L, 1L);
    }

    @Test
    public void testGetExamsByStudentId() throws Exception {
        when(answerService.findExamsIdByWithAnswersByStudent(1L)).thenReturn(Arrays.asList(1L));
        mockMvc.perform(get("/answers/student/1/exams-replied")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
        verify(answerService, times(1)).findExamsIdByWithAnswersByStudent(1L);
    }

    @Test
    public void testUpdate_Success() throws Exception {
        when(answerService.findById("1")).thenReturn(sampleAnswer);
        when(answerService.save(any(Answer.class))).thenReturn(sampleAnswer);
        String json = "{\"text\": \"Updated Answer\", \"studentId\": 1, \"questionId\": 1, \"examId\": 1}";
        mockMvc.perform(put("/answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test Answer"));
        verify(answerService, times(1)).findById("1");
        verify(answerService, times(1)).save(any(Answer.class));
    }

    @Test
    public void testUpdate_NotFound() throws Exception {
        when(answerService.findById("1")).thenReturn(null);
        String json = "{\"text\": \"Updated Answer\"}";
        mockMvc.perform(put("/answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
        verify(answerService, times(1)).findById("1");
    }

    @Test
    public void testDelete_Success() throws Exception {
        when(answerService.findById("1")).thenReturn(sampleAnswer);
        doNothing().when(answerService).deleteById("1");
        mockMvc.perform(delete("/answers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(answerService, times(1)).findById("1");
        verify(answerService, times(1)).deleteById("1");
    }

    @Test
    public void testDelete_NotFound() throws Exception {
        when(answerService.findById("1")).thenReturn(null);
        mockMvc.perform(delete("/answers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(answerService, times(1)).findById("1");
    }
}