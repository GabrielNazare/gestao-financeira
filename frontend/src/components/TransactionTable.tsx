import { useTranslation } from 'react-i18next';
import { Pencil, Trash2, Sparkles } from 'lucide-react';
import type { Transaction } from '../types/transaction';

interface TransactionTableProps {
  transactions: Transaction[];
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

export function TransactionTable({ transactions, onEdit, onDelete }: TransactionTableProps) {
  const { t } = useTranslation();

  if (transactions.length === 0) {
    return (
      <div className="glass-panel p-6 text-center empty-state">
        <Sparkles size={48} className="icon-neon mx-auto mb-4 opacity-50" />
        <p>{t('dashboard.table.empty')}</p>
      </div>
    );
  }

  const sortedTransactions = [...transactions].sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );

  return (
    <div className="glass-panel table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th>{t('dashboard.table.date')}</th>
            <th>{t('dashboard.table.description')}</th>
            <th>{t('dashboard.table.type')}</th>
            <th>{t('dashboard.table.category')}</th>
            <th>{t('dashboard.table.amount')}</th>
            <th style={{ textAlign: 'right' }}>{t('dashboard.table.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {sortedTransactions.map((transaction) => (
            <tr key={transaction.id}>
              <td>{transaction.date ? new Date(transaction.date).toLocaleDateString('pt-BR') : '---'}</td>
              <td>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <span style={{ fontWeight: 500, color: 'var(--text-main)' }}>{transaction.description}</span>
                  {transaction.recurring && (
                    <span className="badge badge-recurring" style={{ width: 'fit-content', marginTop: '4px', fontSize: '10px' }}>
                      {t('dashboard.recurring')}
                    </span>
                  )}
                </div>
              </td>
              <td>
                <span className={`badge ${transaction.type === 'ENTRADA' ? 'badge-income' : 'badge-expense'}`}>
                  {transaction.type === 'ENTRADA' ? t('dashboard.income') : t('dashboard.expense')}
                </span>
              </td>
              <td>{transaction.category}</td>
              <td style={{ fontWeight: 'bold' }}>R$ {transaction.amount.toFixed(2).replace('.', ',')}</td>
              <td>
                <div className="actions" style={{ justifyContent: 'flex-end' }}>
                  <button onClick={() => onEdit(transaction.id)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }} title={t('common.edit')}>
                    <Pencil size={16} />
                  </button>
                  <button onClick={() => onDelete(transaction.id)} style={{ background: 'none', border: 'none', color: 'var(--danger)', cursor: 'pointer' }} title={t('common.delete')}>
                    <Trash2 size={16} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
