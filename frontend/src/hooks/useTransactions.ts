import { useState, useCallback } from 'react';
import { transactionService } from '../services/transactionService';
import type { Transaction, TransactionRequest } from '../types/transaction';

export function useTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const fetchTransactions = useCallback(async () => {
    try {
      const data = await transactionService.getAll();
      setTransactions(data);
    } catch (error) {
      console.error('Erro ao buscar transações:', error);
    }
  }, []);

  const addTransaction = async (request: TransactionRequest) => {
    setIsLoading(true);
    try {
      await transactionService.create(request);
      await fetchTransactions();
    } catch (error) {
      console.error('Erro ao salvar transação:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const updateTransaction = async (id: number, request: TransactionRequest) => {
    setIsLoading(true);
    try {
      await transactionService.update(id, request);
      await fetchTransactions();
    } catch (error) {
      console.error('Erro ao atualizar transação:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const deleteTransaction = async (id: number) => {
    try {
      await transactionService.delete(id);
      await fetchTransactions();
    } catch (error) {
      console.error('Erro ao excluir:', error);
      throw error;
    }
  };

  const uploadStatement = async (file: File) => {
    setIsUploading(true);
    try {
      await transactionService.upload(file);
      await fetchTransactions();
    } catch (error) {
      console.error('Erro no upload do extrato:', error);
      throw error;
    } finally {
      setIsUploading(false);
    }
  };

  const totals = {
    income: transactions.filter(t => t.type === 'ENTRADA').reduce((acc, t) => acc + t.amount, 0),
    expense: transactions.filter(t => t.type === 'SAIDA').reduce((acc, t) => acc + t.amount, 0),
  };

  const balance = totals.income - totals.expense;

  return {
    transactions,
    isLoading,
    isUploading,
    fetchTransactions,
    addTransaction,
    updateTransaction,
    deleteTransaction,
    uploadStatement,
    totals,
    balance,
  };
}
