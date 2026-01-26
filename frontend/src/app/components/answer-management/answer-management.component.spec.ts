import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AnswerManagementComponent } from './answer-management.component';
import { AnswerService } from '../../services/answer.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Answer } from '../../models/Answer';
import { ChangeDetectorRef } from '@angular/core';

describe('AnswerManagementComponent', () => {
  let component: AnswerManagementComponent;
  let fixture: ComponentFixture<AnswerManagementComponent>;
  let answerService: jest.Mocked<AnswerService>;

  const mockAnswers: Answer[] = [
    { id: '1', text: 'Réponse 1', studentId: 1, questionId: 1, examId: 1 },
    { id: '2', text: 'Réponse 2', studentId: 2, questionId: 2, examId: 2 }
  ];

  beforeEach(async () => {
    const answerServiceSpy = {
      getAllAnswers: jest.fn(),
      getAnswersByStudentAndExam: jest.fn(),
      createAnswers: jest.fn(),
      updateAnswer: jest.fn(),
      deleteAnswer: jest.fn()
    };
    const cdrSpy = {
      detectChanges: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [AnswerManagementComponent],
      imports: [HttpClientTestingModule, FormsModule],
      providers: [
        { provide: AnswerService, useValue: answerServiceSpy },
        { provide: ChangeDetectorRef, useValue: cdrSpy }
      ]
    }).compileComponents();

    answerService = TestBed.inject(AnswerService) as jest.Mocked<AnswerService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AnswerManagementComponent);
    component = fixture.componentInstance;
    answerService.getAllAnswers.mockReturnValue(of(mockAnswers));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  

  it('should load all answers on init', fakeAsync(() => {
    answerService.getAllAnswers.mockReturnValue(of(mockAnswers));
    component.ngOnInit();
    tick();
    expect(answerService.getAllAnswers).toHaveBeenCalled();
    expect(component.answers).toEqual(mockAnswers);
    expect(component.message).toBe('2 réponse(s) chargée(s).');
  }));

  it('should search answers by student and exam ID', fakeAsync(() => {
    const filteredAnswers = [mockAnswers[0]];
    answerService.getAnswersByStudentAndExam.mockReturnValue(of(filteredAnswers));
    component.studentIdFilter = 1;
    component.examIdFilter = 1;
    component.searchAnswers();
    tick();
    expect(answerService.getAnswersByStudentAndExam).toHaveBeenCalledWith(1, 1);
    expect(component.answers).toEqual(filteredAnswers);
    expect(component.message).toBe('1 réponse(s) trouvée(s).');
  }));

  it('should show error message when filters are missing', fakeAsync(() => {
    component.studentIdFilter = undefined;
    component.examIdFilter = undefined;
    component.searchAnswers();
    tick();
    expect(answerService.getAnswersByStudentAndExam).not.toHaveBeenCalled();
    expect(component.message).toBe('2 réponse(s) chargée(s).');
  }));

  it('should open add modal with empty answer', () => {
    component.openAddModal();
    expect(component.showModal).toBe(true);
    expect(component.isEditMode).toBe(false);
    expect(component.selectedAnswer).toEqual({ id: '', text: '', studentId: null, questionId: null, examId: null });
  });

  it('should open edit modal with selected answer', () => {
    const answer = mockAnswers[0];
    component.openEditModal(answer);
    expect(component.showModal).toBe(true);
    expect(component.isEditMode).toBe(true);
    expect(component.selectedAnswer).toEqual(answer);
  });

  it('should delete an answer', fakeAsync(() => {
    jest.spyOn(window, 'confirm').mockReturnValue(true);
    answerService.deleteAnswer.mockReturnValue(of(void 0));
    answerService.getAllAnswers.mockReturnValue(of(mockAnswers.filter(a => a.id !== '1')));
    component.deleteAnswer('1');
    tick();
    expect(answerService.deleteAnswer).toHaveBeenCalledWith('1');
    expect(component.answers.length).toBe(1);
    expect(component.message).toBe('1 réponse(s) chargée(s).');
  }));

  it('should create a new answer', fakeAsync(() => {
    const newAnswer = { id: '3', text: 'Réponse 3', studentId: 3, questionId: 3, examId: 3 };
    answerService.createAnswers.mockReturnValue(of([newAnswer]));
    answerService.getAllAnswers.mockReturnValue(of([...mockAnswers, newAnswer]));
    component.selectedAnswer = { id: '', text: 'Réponse 3', studentId: 3, questionId: 3, examId: 3 };
    component.isEditMode = false;
    component.saveAnswer();
    tick();
    expect(answerService.createAnswers).toHaveBeenCalled();
    expect(component.showModal).toBe(false);
    expect(component.message).toBe('3 réponse(s) chargée(s).');
  }));

  it('should update an existing answer', fakeAsync(() => {
    const updatedAnswer = { id: '1', text: 'Réponse modifiée', studentId: 1, questionId: 1, examId: 1 };
    answerService.updateAnswer.mockReturnValue(of(updatedAnswer));
    answerService.getAllAnswers.mockReturnValue(of([updatedAnswer, mockAnswers[1]]));
    component.selectedAnswer = updatedAnswer;
    component.isEditMode = true;
    component.saveAnswer();
    tick();
    expect(answerService.updateAnswer).toHaveBeenCalledWith('1', updatedAnswer);
    expect(component.showModal).toBe(false);
    expect(component.message).toBe('2 réponse(s) chargée(s).');
  }));
});