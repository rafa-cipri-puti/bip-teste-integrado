import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Benefit, PaginatedResponse, TransferRequest, TransferResponse} from '../models/benefit.model';

@Injectable({
    providedIn: 'root'
})
export class BenefitService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api/v1';

    getAll(page: number = 0, size: number = 10): Observable<PaginatedResponse<Benefit>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<PaginatedResponse<Benefit>>(`${this.apiUrl}/beneficios`, { params });
    }

    getById(id: number): Observable<Benefit> {
        return this.http.get<Benefit>(`${this.apiUrl}/beneficios/${id}`);
    }

    create(benefit: Omit<Benefit, 'id'>): Observable<Benefit> {
        return this.http.post<Benefit>(`${this.apiUrl}/beneficios`, benefit);
    }

    update(id: number, benefit: Partial<Benefit>): Observable<Benefit> {
        return this.http.put<Benefit>(`${this.apiUrl}/beneficios/${id}`, benefit);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/beneficios/${id}`);
    }

    transfer(transfer: TransferRequest): Observable<TransferResponse> {
        return this.http.post<TransferResponse>(`${this.apiUrl}/beneficios/transfer`, transfer);
    }
}