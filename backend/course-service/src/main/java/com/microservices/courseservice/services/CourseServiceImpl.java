package com.microservices.courseservice.services;

import com.microservices.commonservice.service.CommonServiceImpl;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.courseservice.clients.AnswerFeignClient;
import com.microservices.courseservice.clients.StudentFeignClient;
import com.microservices.courseservice.models.entity.Course;
import com.microservices.courseservice.models.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl extends CommonServiceImpl<Course, CourseRepository> implements CourseService {

    @Autowired
    private CourseRepository repository; // Injection directe

    private final AnswerFeignClient answerFeignClient;
    private final StudentFeignClient studentFeignClient;

    public CourseServiceImpl(AnswerFeignClient answerFeignClient,
                             StudentFeignClient studentFeignClient) {
        this.answerFeignClient = answerFeignClient;
        this.studentFeignClient = studentFeignClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Course> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Course save(Course course) {
        return repository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course findCourseByStudentId(Long id) {
        return repository.findCourseByStudentId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Long> getExamsIdsWithAnswersByStudentId(Long studentId) {
        return answerFeignClient.getExamsByStudentId(studentId);
    }

    @Override
    public Iterable<Student> getStudentsByCourse(Iterable<Long> ids) {
        return studentFeignClient.getStudentsByCourse(ids);
    }

    @Override
    @Transactional
    public void deleteCourseStudentById(Long studentId) {
        repository.deleteCourseStudentById(studentId); // Supprime l'association dans la base
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findByNameOrDescriptionWithPageable(String text, Pageable pageable) {
        return repository.findByNameOrDescriptionWithPageable(text, pageable);
    }
}