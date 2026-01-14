package com.microservices.examenservice.services;

import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonexam.models.entity.Question;
import com.microservices.commonexam.models.entity.Subject;
import com.microservices.examenservice.models.repository.ExamRepository;
import com.microservices.examenservice.models.repository.SubjectRepository;
import com.microservices.commonservice.exceptions.ResourceNotFoundException; // Import corrigé
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamServiceImplTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam sampleExam;
    private Subject sampleSubject;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
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

        Field repositoryField = examService.getClass().getSuperclass().getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(examService, examRepository);
    }

    @Test
    public void testFindByName_Success() {
        List<Exam> exams = Arrays.asList(sampleExam);
        when(examRepository.findByName("Math")).thenReturn(exams);

        List<Exam> result = examService.findByName("Math");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math Exam", result.get(0).getName());

        verify(examRepository, times(1)).findByName("Math");
    }

    @Test
    public void testFindByName_NoResults() {
        when(examRepository.findByName("Unknown")).thenReturn(Collections.emptyList());

        List<Exam> result = examService.findByName("Unknown");
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(examRepository, times(1)).findByName("Unknown");
    }

    @Test
    public void testFindAllSubjects_Success() {
        List<Subject> subjects = Arrays.asList(sampleSubject);
        when(subjectRepository.findAll()).thenReturn(subjects);

        List<Subject> result = examService.findAllSubjects();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mathematics", result.get(0).getName());

        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    public void testFindAllSubjects_Empty() {
        when(subjectRepository.findAll()).thenReturn(Collections.emptyList());

        List<Subject> result = examService.findAllSubjects();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(subjectRepository, times(1)).findAll();
    }

    @Test
    public void testFindExamsIdWithAnswersByQuestionIds_Success() {
        List<Long> questionIds = Arrays.asList(1L, 2L);
        List<Long> examIds = Arrays.asList(1L, 2L);
        when(examRepository.findExamsIdWithAnswersByQuestionIds(questionIds)).thenReturn(examIds);

        Iterable<Long> result = examService.findExamsIdWithAnswersByQuestionIds(questionIds);
        assertNotNull(result);
        List<Long> resultList = (List<Long>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(1L));

        verify(examRepository, times(1)).findExamsIdWithAnswersByQuestionIds(questionIds);
    }

    @Test
    public void testFindExamsIdWithAnswersByQuestionIds_Empty() {
        List<Long> questionIds = Collections.emptyList();
        when(examRepository.findExamsIdWithAnswersByQuestionIds(questionIds)).thenReturn(Collections.emptyList());

        Iterable<Long> result = examService.findExamsIdWithAnswersByQuestionIds(questionIds);
        assertNotNull(result);
        List<Long> resultList = (List<Long>) result;
        assertTrue(resultList.isEmpty());

        verify(examRepository, times(1)).findExamsIdWithAnswersByQuestionIds(questionIds);
    }

    @Test
    public void testFindByNameWithPageable_Success() {
        Page<Exam> page = new PageImpl<>(Arrays.asList(sampleExam), PageRequest.of(0, 10), 1);
        when(examRepository.findByNameWithPageable("Math", PageRequest.of(0, 10))).thenReturn(page);

        Page<Exam> result = examService.findByNameWithPageable("Math", PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Math Exam", result.getContent().get(0).getName());

        verify(examRepository, times(1)).findByNameWithPageable("Math", PageRequest.of(0, 10));
    }

    @Test
    public void testFindById_Success() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));

        Exam result = examService.findById(1L);
        assertNotNull(result);
        assertEquals("Math Exam", result.getName());

        verify(examRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_NotFound() {
        when(examRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.findById(1L));

        verify(examRepository, times(1)).findById(1L);
    }

    @Test
    public void testSave_Success() {
        when(examRepository.save(sampleExam)).thenReturn(sampleExam);

        Exam result = examService.save(sampleExam);
        assertNotNull(result);
        assertEquals("Math Exam", result.getName());

        verify(examRepository, times(1)).save(sampleExam);
    }
}