import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Save, Loader2 } from 'lucide-react';
import type { TransactionRequest, TransactionType } from '../types/transaction';

interface TransactionFormProps {
  initialData?: TransactionRequest;
  onSubmit: (data: TransactionRequest) => Promise<void>;
  isLoading: boolean;
  buttonText?: string;
  isEdit?: boolean;
}

export function TransactionForm({ 
  initialData, 
  onSubmit, 
  isLoading, 
  buttonText,
  isEdit = false
}: TransactionFormProps) {
  const { t } = useTranslation();
  const [description, setDescription] = useState(initialData?.description || '');
  const [amount, setAmount] = useState(initialData?.amount?.toString() || '');
  const [date, setDate] = useState(initialData?.date ? initialData.date.split('T')[0] : '');
  const [type, setType] = useState<TransactionType>(initialData?.type || 'SAIDA');
  const [isRecurring, setIsRecurring] = useState(initialData?.recurring || false);

  useEffect(() => {
    if (initialData) {
      setDescription(initialData.description);
      setAmount(initialData.amount.toString());
      setDate(initialData.date ? initialData.date.split('T')[0] : '');
      setType(initialData.type || 'SAIDA');
      setIsRecurring(initialData.recurring);
    }
  }, [initialData]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!description.trim() || !amount || parseFloat(amount) <= 0) {
      alert(t('dashboard.invalid_input_alert'));
      return;
    }

    const request: TransactionRequest = {
      description,
      amount: parseFloat(amount),
      date: date ? date + "T00:00:00" : undefined,
      type,
      recurring: isRecurring
    };

    await onSubmit(request);
    
    if (!isEdit) {
      setDescription('');
      setAmount('');
      setDate('');
      setType('SAIDA');
      setIsRecurring(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="expense-form">
      <div className="form-group">
        <label>{t('dashboard.description')}</label>
        <input
          type="text"
          className="input-glass"
          placeholder="Ex: Uber, Mercado, Netflix"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
        />
      </div>
      <div className="form-group">
        <label>{t('dashboard.amount')}</label>
        <input
          type="number"
          step="0.01"
          min="0.01"
          className="input-glass"
          placeholder="0.00"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
      </div>
      {isEdit && (
        <div className="form-group">
          <label>{t('dashboard.table.date')}</label>
          <input
            type="date"
            className="input-glass"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            required
          />
        </div>
      )}
      <div style={{ display: 'flex', gap: '1rem' }}>
        <div className="form-group" style={{ flex: 1 }}>
          <label>{t('dashboard.type')}</label>
          <select className="input-glass" value={type} onChange={(e) => setType(e.target.value as TransactionType)}>
            <option value="SAIDA">{t('dashboard.expense')}</option>
            <option value="ENTRADA">{t('dashboard.income')}</option>
          </select>
        </div>
        <div className="form-group" style={{ flex: 1 }}>
          <label>{t('dashboard.recurring')}</label>
          <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
            <input
              type="checkbox"
              checked={isRecurring}
              onChange={(e) => setIsRecurring(e.target.checked)}
              style={{ width: '1.2rem', height: '1.2rem', accentColor: 'var(--accent-neon)' }}
            />
            <span style={{ marginLeft: '0.5rem', color: 'var(--text-muted)' }}>{t('dashboard.yes')}</span>
          </div>
        </div>
      </div>
      <button type="submit" className="btn-primary w-full" disabled={isLoading}>
        {isLoading ? <Loader2 className="spinner" /> : (isEdit ? <Save size={20} /> : <Plus size={20} />)}
        {isLoading ? t('ai_assistant.thinking') : (buttonText || t('dashboard.add_expense'))}
      </button>
    </form>
  );
}
