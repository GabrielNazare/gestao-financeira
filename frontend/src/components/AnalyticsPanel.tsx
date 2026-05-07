import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell, PieChart, Pie, Legend } from 'recharts';
import { useTranslation } from 'react-i18next';
import type { Transaction } from '../types/transaction';

interface AnalyticsPanelProps {
  transactions: Transaction[];
}

export function AnalyticsPanel({ transactions }: AnalyticsPanelProps) {
  const { t } = useTranslation();

  const daysOfWeek = [
    t('analytics.days.sun'), t('analytics.days.mon'), t('analytics.days.tue'),
    t('analytics.days.wed'), t('analytics.days.thu'), t('analytics.days.fri'), t('analytics.days.sat')
  ];
  const weekData = daysOfWeek.map(day => ({ name: day, total: 0 }));

  const monthsOfYear = [
    t('analytics.months.jan'), t('analytics.months.feb'), t('analytics.months.mar'),
    t('analytics.months.apr'), t('analytics.months.may'), t('analytics.months.jun'),
    t('analytics.months.jul'), t('analytics.months.aug'), t('analytics.months.sep'),
    t('analytics.months.oct'), t('analytics.months.nov'), t('analytics.months.dec')
  ];
  const monthByMonthDataMap = new Map<number, number>();
  for (let i = 0; i < 12; i++) {
    monthByMonthDataMap.set(i, 0);
  }

  transactions.filter(t => t.type === 'SAIDA').forEach(t => {
    const d = new Date(t.date);
    const dayOfWeek = d.getDay();
    weekData[dayOfWeek].total += t.amount;

    const month = d.getMonth();
    const currentMonthTotal = monthByMonthDataMap.get(month) || 0;
    monthByMonthDataMap.set(month, currentMonthTotal + t.amount);
  });

  const monthData = Array.from(monthByMonthDataMap.entries())
    .map(([monthIndex, total]) => ({ name: monthsOfYear[monthIndex], total }))
    .filter(item => item.total > 0);

  const expensesByCategory = new Map<string, number>();
  transactions.filter(t => t.type === 'SAIDA').forEach(t => {
    const current = expensesByCategory.get(t.category) || 0;
    expensesByCategory.set(t.category, current + t.amount);
  });

  const categoryData = Array.from(expensesByCategory.entries())
    .map(([name, value]) => ({ name, value }))
    .filter(item => item.value > 0);

  const COLORS = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      const displayName = label || payload[0].name;
      return (
        <div className="glass-panel" style={{ padding: '8px 12px', fontSize: '0.875rem' }}>
          <p style={{ color: 'var(--text-muted)' }}>{displayName}</p>
          <p style={{ color: 'var(--accent-neon)', fontWeight: 'bold' }}>
            R$ {payload[0].value.toFixed(2).replace('.', ',')}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', height: '100%' }}>
      <div className="glass-panel" style={{ padding: '1.5rem', flex: 1 }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--text-main)', fontSize: '1.1rem' }}>{t('analytics.week_chart')}</h3>
        <div style={{ height: '200px', width: '100%' }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weekData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
              <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} />
              <YAxis stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `R$${value}`} />
              <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.05)' }} />
              <Bar dataKey="total" radius={[4, 4, 0, 0]}>
                {weekData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.total > 0 ? 'var(--accent-neon)' : 'transparent'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="glass-panel" style={{ padding: '1.5rem', flex: 1 }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--text-main)', fontSize: '1.1rem' }}>{t('analytics.monthly_evolution')}</h3>
        {monthData.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', textAlign: 'center', marginTop: '2rem' }}>
            {t('analytics.no_data')}
          </p>
        ) : (
          <div style={{ height: '200px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `R$${value}`} />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.05)' }} />
                <Bar dataKey="total" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="glass-panel" style={{ padding: '1.5rem', flex: 1 }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--text-main)', fontSize: '1.1rem' }}>{t('analytics.category_chart')}</h3>
        {categoryData.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', textAlign: 'center', marginTop: '2rem' }}>
            {t('analytics.no_data')}
          </p>
        ) : (
          <div style={{ height: '200px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="45%"
                  innerRadius={50}
                  outerRadius={70}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {categoryData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  layout="horizontal"
                  verticalAlign="bottom"
                  align="center"
                  iconType="circle"
                  wrapperStyle={{ fontSize: '0.8rem', marginTop: '10px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
}

