import { Injectable } from '@angular/core';
import { CommonService } from './common.service';
import { Course } from '../models/Course';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs"; // Ajouté

@Injectable({
  providedIn: 'root'
})
export class CourseService extends CommonService<Course> {

  protected baseEnpoint = `${environment.API_URL}/courses`;

  constructor(http: HttpClient) {
    super(http);
  }

  // --- MÉTHODE AJOUTÉE POUR CORRIGER L'ERREUR ---
  public getAllPagesWithText(page: string, size: string, text: string): Observable<any> {
    return this.http.get<any>(`${this.baseEnpoint}/page/${page}/${size}/${text}`);
  }
}