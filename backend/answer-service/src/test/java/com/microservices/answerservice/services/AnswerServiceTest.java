package com.microservices.answerservice.services;

import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.models.repository.AnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository; // Simule le repository MongoDB

    @InjectMocks
    private AnswerServiceImpl answerService; // Service testé

    private Answer sampleAnswer;

    @BeforeEach
    public void setUp() {
        // Initialise les mocks
        MockitoAnnotations.openMocks(this);

        // Prépare une réponse pour les tests
        sampleAnswer = new Answer();
        sampleAnswer.setId("1");
        sampleAnswer.setText("Test Answer");
        sampleAnswer.setStudentId(1L);
        sampleAnswer.setQuestionId(1L);
        sampleAnswer.setExamId(1L);
    }

    @Test
    public void testSaveAll() {
        // Simule le repository
        when(answerRepository.saveAll(anyIterable())).thenReturn(Arrays.asList(sampleAnswer));

        // Teste la méthode
        Iterable<Answer> answers = answerService.saveAll(Arrays.asList(sampleAnswer));

        // Vérifie
        assertNotNull(answers);
        assertEquals("1", ((Answer) answers.iterator().next()).getId());
        verify(answerRepository, times(1)).saveAll(anyIterable());
    }

    @Test
    public void testSave() {
        // Simule le repository
        when(answerRepository.save(any(Answer.class))).thenReturn(sampleAnswer);

        // Teste la méthode
        Answer savedAnswer = answerService.save(sampleAnswer);

        // Vérifie
        assertNotNull(savedAnswer);
        assertEquals("1", savedAnswer.getId());
        verify(answerRepository, times(1)).save(any(Answer.class));
    }

    @Test
    public void testFindAnswerByStudentByExam() {
        // Simule le repository
        when(answerRepository.findAnswerByStudentByExam(1L, 1L)).thenReturn(Arrays.asList(sampleAnswer));

        // Teste la méthode
        Iterable<Answer> answers = answerService.findAnswerByStudentByExam(1L, 1L);

        // Vérifie
        assertNotNull(answers);
        assertEquals("1", ((Answer) answers.iterator().next()).getId());
        verify(answerRepository, times(1)).findAnswerByStudentByExam(1L, 1L);
    }

    @Test
    public void testFindExamsIdByWithAnswersByStudent() {
        // Simule le repository
        when(answerRepository.findExamsIdByWithAnswersByStudent(1L)).thenReturn(Arrays.asList(sampleAnswer));

        // Teste la méthode
        Iterable<Long> examIds = answerService.findExamsIdByWithAnswersByStudent(1L);

        // Vérifie
        assertNotNull(examIds);
        assertEquals(1L, examIds.iterator().next());
        verify(answerRepository, times(1)).findExamsIdByWithAnswersByStudent(1L);
    }

    @Test
    public void testFindById_Success() {
        // Simule le repository
        when(answerRepository.findById("1")).thenReturn(Optional.of(sampleAnswer));

        // Teste la méthode
        Answer answer = answerService.findById("1");

        // Vérifie
        assertNotNull(answer);
        assertEquals("1", answer.getId());
        verify(answerRepository, times(1)).findById("1");
    }

    @Test
    public void testFindById_NotFound() {
        // Simule le repository
        when(answerRepository.findById("1")).thenReturn(Optional.empty());

        // Teste la méthode
        Answer answer = answerService.findById("1");

        // Vérifie
        assertNull(answer);
        verify(answerRepository, times(1)).findById("1");
    }

    @Test
    public void testDeleteById() {
        // Simule le repository
        doNothing().when(answerRepository).deleteById("1");

        // Teste la méthode
        answerService.deleteById("1");

        // Vérifie
        verify(answerRepository, times(1)).deleteById("1");
    }

    @Test
    public void testFindByStudentId() {
        // Simule le repository
        when(answerRepository.findByStudentId(1L)).thenReturn(Arrays.asList(sampleAnswer));

        // Teste la méthode
        Iterable<Answer> answers = answerService.findByStudentId(1L);

        // Vérifie
        assertNotNull(answers);
        assertEquals("1", ((Answer) answers.iterator().next()).getId());
        verify(answerRepository, times(1)).findByStudentId(1L);
    }

    @Test
    public void testFindAll() {
        // Simule le repository
        when(answerRepository.findAll()).thenReturn(Arrays.asList(sampleAnswer));

        // Teste la méthode
        Iterable<Answer> answers = answerService.findAll();

        // Vérifie
        assertNotNull(answers);
        assertEquals("1", ((Answer) answers.iterator().next()).getId());
        verify(answerRepository, times(1)).findAll();
    }
}