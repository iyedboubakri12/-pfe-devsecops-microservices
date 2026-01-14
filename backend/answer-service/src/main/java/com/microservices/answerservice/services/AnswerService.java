package com.microservices.answerservice.services;

import com.microservices.answerservice.models.entity.Answer;

public interface AnswerService {
    Iterable<Answer> saveAll(Iterable<Answer> answers);
    Answer save(Answer answer);
    Iterable<Answer> findAnswerByStudentByExam(Long studentId, Long examId);
    Iterable<Long> findExamsIdByWithAnswersByStudent(Long studentId);
    Answer findById(String id);
    void deleteById(String id);
    Iterable<Answer> findByStudentId(Long studentId);
    Iterable<Answer> findAll(); // Nouvelle méthode ajoutée
}
