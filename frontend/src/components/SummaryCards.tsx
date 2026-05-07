import { useTranslation } from 'react-i18next';

interface SummaryCardsProps {
  income: number;
  expense: number;
  balance: number;
}

export function SummaryCards({ income, expense, balance }: SummaryCardsProps) {
  const { t } = useTranslation();

  const formatCurrency = (value: number) => {
    return `R$ ${value.toFixed(2).replace('.', ',')}`;
  };

  return (
    <div className="metrics-grid">
      <div className="glass-panel metric-card">
        <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>{t('dashboard.metrics.income')}</span>
        <span className="metric-value income">{formatCurrency(income)}</span>
      </div>
      <div className="glass-panel metric-card">
        <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>{t('dashboard.metrics.expense')}</span>
        <span className="metric-value expense">{formatCurrency(expense)}</span>
      </div>
      <div className="glass-panel metric-card">
        <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>{t('dashboard.metrics.balance')}</span>
        <span className="metric-value" style={{ color: balance >= 0 ? '#10b981' : '#ef4444' }}>
          {formatCurrency(balance)}
        </span>
      </div>
    </div>
  );
}
