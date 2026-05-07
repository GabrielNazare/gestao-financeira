export type TransactionType = 'ENTRADA' | 'SAIDA';

export interface Transaction {
  id: number;
  description: string;
  amount: number;
  date: string;
  category: string;
  confidence: number;
  type: TransactionType;
  recurring: boolean;
}

export interface TransactionRequest {
  description: string;
  amount: number;
  date?: string;
  type?: TransactionType;
  recurring: boolean;
}
