import React, { useState, useEffect, useRef } from 'react';
import { AnalyticsPanel } from '../components/AnalyticsPanel';
import { AiAssistant } from '../components/AiAssistant';
import { SummaryCards } from '../components/SummaryCards';
import { TransactionTable } from '../components/TransactionTable';
import { TransactionForm } from '../components/TransactionForm';
import { LogOut, Sparkles, Loader2, X, UploadCloud, Languages } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTransactions } from '../hooks/useTransactions';
import { setAuthToken } from '../services/api';
import './Dashboard.css';

interface DashboardProps {
  token: string;
  onLogout: () => void;
}

export function Dashboard({ token, onLogout }: DashboardProps) {
  const { t, i18n } = useTranslation();
  const {
    transactions,
    isLoading,
    isUploading,
    fetchTransactions,
    addTransaction,
    updateTransaction,
    deleteTransaction,
    uploadStatement,
    totals,
    balance
  } = useTransactions();

  const [editingId, setEditingId] = useState<number | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (token) {
      setAuthToken(token);
      fetchTransactions();
    }
  }, [token, fetchTransactions]);

  const toggleLanguage = () => {
    const newLang = i18n.language === 'pt-BR' ? 'en' : 'pt-BR';
    i18n.changeLanguage(newLang);
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      await uploadStatement(file);
      alert(t('dashboard.upload_success'));
    } catch (error: any) {
      const serverMsg = error.response?.data?.message || '';
      alert(`${t('dashboard.upload_error')}: ${serverMsg}`);
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleEdit = (id: number) => {
    setEditingId(id);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm(t('common.confirm_delete'))) return;
    try {
      await deleteTransaction(id);
    } catch (error) {
      alert(t('common.error_delete'));
    }
  };

  const closeEditModal = () => setEditingId(null);

  const editingTransaction = transactions.find(t => t.id === editingId);

  return (
    <div className="dashboard-container">
      <header className="dashboard-header glass-panel">
        <div className="logo">
          <Sparkles className="icon-neon" />
          <span className="text-gradient font-bold">FinAI Dashboard</span>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button
            onClick={toggleLanguage}
            className="btn-logout"
            style={{ padding: '0.5rem 0.8rem', display: 'flex', alignItems: 'center', gap: '6px' }}
            title="Switch Language"
          >
            <Languages size={16} />
            <span style={{ fontWeight: 'bold', fontSize: '0.8rem' }}>{i18n.language.toUpperCase()}</span>
          </button>
          <button onClick={onLogout} className="btn-logout">
            <LogOut size={18} /> {t('common.logout')}
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-grid">
          
          <div className="column-left">
            <div className="glass-panel p-6 mb-4">
              <h2 className="section-title mb-1">{t('dashboard.add_expense')}</h2>
              <p className="section-subtitle mb-4">{t('dashboard.ai_hint')}</p>

              <TransactionForm 
                onSubmit={addTransaction} 
                isLoading={isLoading} 
              />

              <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-glass)' }}>
                <p className="section-subtitle" style={{ textAlign: 'center', marginBottom: '1rem' }}>
                  {t('dashboard.upload_hint')}
                </p>
                <input
                  type="file"
                  accept=".pdf,.csv"
                  style={{ display: 'none' }}
                  ref={fileInputRef}
                  onChange={handleFileUpload}
                />
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="btn-primary w-full"
                  style={{ background: 'transparent', border: '1px dashed var(--accent-neon)', color: 'var(--accent-neon)', boxShadow: 'none' }}
                  disabled={isUploading || isLoading}
                >
                  {isUploading ? <Loader2 className="spinner" size={20} /> : <UploadCloud size={20} />}
                  {isUploading ? t('ai_assistant.thinking') : t('dashboard.upload_button')}
                </button>
              </div>
            </div>

            <SummaryCards 
              income={totals.income} 
              expense={totals.expense} 
              balance={balance} 
            />

            <div className="transactions-list-section">
              <h2 className="section-title mb-4">{t('dashboard.table.title')}</h2>
              <TransactionTable 
                transactions={transactions} 
                onEdit={handleEdit} 
                onDelete={handleDelete} 
              />
            </div>
          </div>

          <div className="column-middle">
            <AnalyticsPanel transactions={transactions} />
          </div>

          <div className="column-right">
            <AiAssistant />
          </div>

        </div>
      </main>

      {editingId && editingTransaction && (
        <div className="modal-overlay" onClick={closeEditModal}>
          <div className="modal-content glass-panel" onClick={(e) => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 className="section-title" style={{ margin: 0 }}>{t('dashboard.edit_title')}</h2>
              <button onClick={closeEditModal} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <TransactionForm 
              isEdit 
              initialData={editingTransaction}
              onSubmit={async (data) => {
                await updateTransaction(editingId, data);
                closeEditModal();
              }} 
              isLoading={isLoading}
              buttonText={t('common.save')}
            />
          </div>
        </div>
      )}
    </div>
  );
}
