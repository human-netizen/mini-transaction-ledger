export type EntryType = 'CREDIT' | 'DEBIT';

export interface LedgerEntry {
  id: number;
  type: EntryType;
  amount: number;
  description: string;
  balanceAfter: number;
  createdAt: string;
}

export interface CreateEntryRequest {
  type: EntryType;
  amount: number;
  description: string;
}
