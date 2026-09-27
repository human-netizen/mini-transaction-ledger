import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateEntryRequest, LedgerEntry } from '../models/ledger-entry.model';

@Injectable({ providedIn: 'root' })
export class LedgerService {
  private readonly http = inject(HttpClient);

  getEntries(accountId: number): Observable<LedgerEntry[]> {
    return this.http.get<LedgerEntry[]>(`/api/accounts/${accountId}/entries`);
  }

  addEntry(accountId: number, request: CreateEntryRequest): Observable<LedgerEntry> {
    return this.http.post<LedgerEntry>(`/api/accounts/${accountId}/entries`, request);
  }
}
