export class Answer {
  id: string = '';
  text: string = '';
  student?: any; // Optionnel
  question?: any; // Optionnel
  studentId?: number | null;
  questionId?: number | null;
  examId?: number | null;
}
