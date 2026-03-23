import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { Generic } from '../models/Generic';

@Injectable({
  providedIn: 'root'
})
export abstract class CommonService<E extends Generic> {

  protected baseEnpoint: string;

  constructor(protected http: HttpClient) {}

  private getHeaders(): HttpHeaders {

    const token = localStorage.getItem('token');

    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    });
  }

  public getAll(): Observable<E[]> {
    return this.http.get<E[]>(`${this.baseEnpoint}`, { headers: this.getHeaders() });
  }

  public getAllPages(page: string, size: string): Observable<any> {
    return this.http.get<any>(`${this.baseEnpoint}/page/${page}/${size}`, { headers: this.getHeaders() });
  }

  public getById(id: number): Observable<E> {
    return this.http.get<E>(`${this.baseEnpoint}/${id}`, { headers: this.getHeaders() });
  }

  public create(data: E): Observable<E> {
    return this.http.post<E>(`${this.baseEnpoint}/`, data, { headers: this.getHeaders() });
  }

  public update(data: E): Observable<E> {
    return this.http.put<E>(`${this.baseEnpoint}/${data['id']}`, data, { headers: this.getHeaders() });
  }

  public delete(id: number): Observable<E> {
    return this.http.delete<E>(`${this.baseEnpoint}/${id}`, { headers: this.getHeaders() });
  }
}
