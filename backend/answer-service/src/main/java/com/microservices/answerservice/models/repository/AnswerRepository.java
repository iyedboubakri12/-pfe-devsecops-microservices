package com.microservices.answerservice.models.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.microservices.answerservice.models.entity.Answer;

public interface AnswerRepository extends MongoRepository<Answer, String> {

    @Query("{'studentId': ?0, 'questionId': {$in: ?1} }")
    Iterable<Answer> findAnswerByStudentByQuestionIds(Long studentId, Iterable<Long> questionIds);

    @Query("{'studentId': ?0}")
    Iterable<Answer> findByStudentId(Long studentId);

    @Query("{'studentId': ?0, 'examId': ?1}")
    Iterable<Answer> findAnswerByStudentByExam(Long studentId, Long examId);

    @Query(value = "{'studentId': ?0}", fields = "{'examId': 1}")
    Iterable<Answer> findExamsIdByWithAnswersByStudent(Long studentId);
}
