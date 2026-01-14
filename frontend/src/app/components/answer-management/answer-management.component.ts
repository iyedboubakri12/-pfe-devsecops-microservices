import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AnswerService } from '../../services/answer.service';
import { Answer } from '../../models/Answer';

@Component({
  selector: 'app-answer-management',
  templateUrl: './answer-management.component.html',
  styleUrls: ['./answer-management.component.css']
})
export class AnswerManagementComponent implements OnInit {
  answers: Answer[] = [];
  studentIdFilter?: number;
  examIdFilter?: number;
  showModal: boolean = false;
  selectedAnswer: Answer = { id: '', text: '', studentId: null, questionId: null, examId: null };
  isEditMode: boolean = false;
  message: string = '';
  component: { text: string; studentId: number; questionId: number; examId: number; };

  constructor(
    private answerService: AnswerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAllAnswers(); // Charge toutes les réponses au démarrage
  }

  loadAllAnswers() {
    this.answerService.getAllAnswers().subscribe({
      next: (data) => {
        this.answers = data || [];
        this.cdr.detectChanges(); // Force la mise à jour de l'affichage
        this.message = this.answers.length > 0 ? `${this.answers.length} réponse(s) chargée(s).` : 'Aucune réponse disponible.';
      },
      error: (err) => {
        this.message = `Erreur : ${err.message}`;
        this.answers = [];
        this.cdr.detectChanges();
      }
    });
  }

  searchAnswers() {
    if (this.studentIdFilter && this.examIdFilter) {
      this.answerService.getAnswersByStudentAndExam(this.studentIdFilter, this.examIdFilter).subscribe({
        next: (data) => {
          this.answers = data || [];
          this.cdr.detectChanges();
          this.message = data.length > 0 ? `${data.length} réponse(s) trouvée(s).` : 'Aucune réponse trouvée.';
        },
        error: (err) => {
          this.message = `Erreur : ${err.message}`;
          this.loadAllAnswers();
        }
      });
    } else {
      this.message = 'Veuillez entrer un ID étudiant et un ID examen.';
      this.loadAllAnswers();
    }
  }

  openAddModal() {
    this.selectedAnswer = { id: '', text: '', studentId: null, questionId: null, examId: null };
    this.isEditMode = false;
    this.showModal = true;
  }

  openEditModal(answer: Answer) {
    this.selectedAnswer = { ...answer };
    this.isEditMode = true;
    this.showModal = true;
  }

  saveAnswer() {
    if (this.isEditMode) {
      this.answerService.updateAnswer(this.selectedAnswer.id!, this.selectedAnswer).subscribe({
        next: (data) => {
          this.message = 'Réponse modifiée avec succès !';
          this.updateLocalAnswer(data);
          this.showModal = false;
          this.loadAllAnswers();
        },
        error: (err) => {
          this.message = `Erreur : ${err.message}`;
          this.showModal = false;
        }
      });
    } else {
      this.answerService.createAnswers([this.selectedAnswer]).subscribe({
        next: (data) => {
          this.message = 'Réponse ajoutée avec succès !';
          this.answers.push(data[0]);
          this.showModal = false;
          this.loadAllAnswers();
        },
        error: (err) => {
          this.message = `Erreur : ${err.message}`;
          this.showModal = false;
        }
      });
    }
  }

  deleteAnswer(id: string) {
    if (confirm('Voulez-vous vraiment supprimer cette réponse ?')) {
      this.answerService.deleteAnswer(id).subscribe({
        next: () => {
          this.message = 'Réponse supprimée avec succès !';
          this.answers = this.answers.filter(a => a.id !== id);
          this.loadAllAnswers();
        },
        error: (err) => {
          this.message = `Erreur : ${err.message}`;
        }
      });
    }
  }

  private updateLocalAnswer(updatedAnswer: Answer) {
    const index = this.answers.findIndex(a => a.id === updatedAnswer.id);
    if (index !== -1) this.answers[index] = updatedAnswer;
  }

  closeModal() {
    this.showModal = false;
    this.message = '';
  }
  
  
}
