package com.microservices.users.services;

import com.microservices.commonstudent.models.entity.Student;
import com.microservices.commonservice.exceptions.ResourceNotFoundException;
import com.microservices.users.clients.CourseFeignClient;
import com.microservices.users.models.repository.StudentRepository;
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
public class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseFeignClient courseFeignClient;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student sampleStudent;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        sampleStudent = new Student();
        sampleStudent.setId(1L);
        sampleStudent.setName("John");
        sampleStudent.setLastName("Doe");
        sampleStudent.setEmail("john.doe@example.com");
        sampleStudent.setImage(new byte[]{1, 2, 3});

        // Injecter le repository dans la classe parent CommonServiceImpl
        Field repositoryField = studentService.getClass().getSuperclass().getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(studentService, studentRepository);
    }

    @Test
    public void testFindByNameAndLastName_Success() {
        when(studentRepository.findByNameAndLastName("john")).thenReturn(Collections.singletonList(sampleStudent));

        List<Student> result = studentService.findByNameAndLastName("john");
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    public void testFindByNameAndLastName_Empty() {
        when(studentRepository.findByNameAndLastName("unknown")).thenReturn(Collections.emptyList());

        List<Student> result = studentService.findByNameAndLastName("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByNameAndLastNameWithPageable_Success() {
        Page<Student> page = new PageImpl<>(Collections.singletonList(sampleStudent));
        when(studentRepository.findByNameAndLastNameWithPageable("john", PageRequest.of(0, 10))).thenReturn(page);

        Page<Student> result = studentService.findByNameAndLastNameWithPageable("john", PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getName());
    }

    @Test
    public void testFindAllById_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(studentRepository.findAllById(ids)).thenReturn(Collections.singletonList(sampleStudent));

        Iterable<Student> result = studentService.findAllById(ids);
        List<Student> resultList = (List<Student>) result;
        assertEquals(1, resultList.size());
        assertEquals("John", resultList.get(0).getName());
    }

    @Test
    public void testFindAllById_Empty() {
        List<Long> ids = Collections.emptyList();
        when(studentRepository.findAllById(ids)).thenReturn(Collections.emptyList());

        Iterable<Student> result = studentService.findAllById(ids);
        List<Student> resultList = (List<Student>) result;
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void testFindById_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

        Student result = studentService.findById(1L);
        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    public void testFindById_NotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findById(1L));
    }

    @Test
    public void testFindAll_Success() {
        when(studentRepository.findAll()).thenReturn(Collections.singletonList(sampleStudent));

        Iterable<Student> result = studentService.findAll();
        List<Student> resultList = (List<Student>) result;
        assertEquals(1, resultList.size());
        assertEquals("John", resultList.get(0).getName());
    }

    @Test
    public void testFindAllPage_Success() {
        Page<Student> page = new PageImpl<>(Collections.singletonList(sampleStudent));
        when(studentRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<Student> result = studentService.findAllPage(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getName());
    }

    @Test
    public void testSave_Success() {
        when(studentRepository.save(sampleStudent)).thenReturn(sampleStudent);

        Student result = studentService.save(sampleStudent);
        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    public void testUpdate_Success() {
        // update() appelle save(), donc on teste save ici
        when(studentRepository.save(sampleStudent)).thenReturn(sampleStudent);

        Student result = studentService.update(sampleStudent);
        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    public void testDeleteById_Success() {
        doNothing().when(studentRepository).deleteById(1L);
        doNothing().when(courseFeignClient).deleteCourseByStudentId(1L);

        studentService.deleteById(1L);

        verify(studentRepository, times(1)).deleteById(1L);
        verify(courseFeignClient, times(1)).deleteCourseByStudentId(1L);
    }

    @Test
    public void testDeleteCourseStudentById_Success() {
        doNothing().when(courseFeignClient).deleteCourseByStudentId(1L);

        studentService.deleteCourseStudentById(1L);

        verify(courseFeignClient, times(1)).deleteCourseByStudentId(1L);
    }
}
