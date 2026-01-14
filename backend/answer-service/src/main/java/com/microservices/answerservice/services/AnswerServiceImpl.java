package com.microservices.answerservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microservices.answerservice.models.entity.Answer;
import com.microservices.answerservice.models.repository.AnswerRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerRepository repository;

    @Override
    public Iterable<Answer> saveAll(Iterable<Answer> answers) {
        return repository.saveAll(answers);
    }

    @Override
    public Answer save(Answer answer) {
        return repository.save(answer);
    }

    @Override
    public Iterable<Answer> findAnswerByStudentByExam(Long studentId, Long examId) {
        return repository.findAnswerByStudentByExam(studentId, examId);
    }

    @Override
    public Iterable<Long> findExamsIdByWithAnswersByStudent(Long studentId) {
        Iterable<Answer> answers = repository.findExamsIdByWithAnswersByStudent(studentId);
        List<Long> examIds = new ArrayList<>();
        answers.forEach(answer -> examIds.add(answer.getExamId()));
        return examIds;
    }

    @Override
    public Answer findById(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public Iterable<Answer> findByStudentId(Long studentId) {
        return repository.findByStudentId(studentId);
    }

    @Override
    public Iterable<Answer> findAll() { // Nouvelle implémentation ajoutée
        return repository.findAll();
    }
}
