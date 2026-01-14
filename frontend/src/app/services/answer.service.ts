import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Answer } from '../models/Answer';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {
  private apiUrl = 'http://localhost:8090/answers'; // URL du backend via Zuul

  constructor(private http: HttpClient) {}

  getAllAnswers(): Observable<Answer[]> {
    return this.http.get<Answer[]>(this.apiUrl);
  }

  getAnswersByStudentAndExam(studentId: number, examId: number): Observable<Answer[]> {
    return this.http.get<Answer[]>(`${this.apiUrl}/student/${studentId}/exam/${examId}`);
  }

  createAnswers(answers: Answer[]): Observable<Answer[]> {
    const payload = answers.map(a => ({
      text: a.text,
      studentId: a.studentId,
      questionId: a.questionId,
      examId: a.examId
    }));
    return this.http.post<Answer[]>(this.apiUrl, payload);
  }

  updateAnswer(id: string, answer: Answer): Observable<Answer> {
    const payload = {
      text: answer.text,
      studentId: answer.studentId,
      questionId: answer.questionId,
      examId: answer.examId
    };
    return this.http.put<Answer>(`${this.apiUrl}/${id}`, payload);
  }

  deleteAnswer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
