import { Injectable } from '@angular/core';
import { CommonService } from './common.service';
import { Exam } from '../models/Exam';
import { environment } from 'src/environments/environment';
import { Subject } from '../models/Subject';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ExamService extends CommonService<Exam> {
  protected baseEnpoint = `${environment.API_URL}/exams`;

  constructor(http: HttpClient){
    super(http);
  }

  // --- MÉTHODE AJOUTÉE POUR CORRIGER L'ERREUR ---
  public getAllPagesWithText(page: string, size: string, text: string): Observable<any> {
    return this.http.get<any>(`${this.baseEnpoint}/page/${page}/${size}/${text}`);
  }

  public getAlllSubjects(): Observable<Subject[]>{
    return this.http.get<Subject[]>(`${this.baseEnpoint}/subjects`);
  }
}