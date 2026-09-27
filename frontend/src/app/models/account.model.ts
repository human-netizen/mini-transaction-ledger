export interface Account {
  id: number;
  holderName: string;
  balance: number;
  createdAt: string;
}

export interface CreateAccountRequest {
  holderName: string;
}
