import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Stock } from '../models/stock.model';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private http = inject(HttpClient);
  private apiUrl = '/api/stocks';

  listarExistencias(): Observable<Stock[]> {
    return this.http.get<Stock[]>(this.apiUrl);
  }
}
