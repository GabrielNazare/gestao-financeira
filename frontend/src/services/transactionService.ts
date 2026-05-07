import api from './api';
import type { Transaction, TransactionRequest } from '../types/transaction';

export const transactionService = {
  getAll: async (): Promise<Transaction[]> => {
    const response = await api.get<Transaction[]>('/transactions');
    return response.data;
  },

  create: async (transaction: TransactionRequest): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transactions', transaction);
    return response.data;
  },

  update: async (id: number, transaction: TransactionRequest): Promise<Transaction> => {
    const response = await api.put<Transaction>(`/transactions/${id}`, transaction);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/transactions/${id}`);
  },

  upload: async (file: File): Promise<Transaction[]> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<Transaction[]>('/transactions/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
