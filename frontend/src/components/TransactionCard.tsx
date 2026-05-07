import { Tag, Calendar, Sparkles, Pencil, Trash2 } from 'lucide-react';

interface TransactionProps {
  id: number;
  description: string;
  amount: number;
  date: string;
  category: string;
  confidence: number;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

export function TransactionCard({ id, description, amount, date, category, confidence, onEdit, onDelete }: TransactionProps) {
  const formattedDate = new Date(date).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  });

  return (
    <div className="glass-panel transaction-card-wrapper" style={{ padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', position: 'relative', gap: '1rem' }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', minWidth: 0, flex: 1 }}>
        <h3 style={{ fontSize: '1.1rem', margin: 0, color: 'var(--text-main)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={description}>
          {description}
        </h3>
        <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-muted)', flexWrap: 'wrap' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', whiteSpace: 'nowrap' }}>
            <Calendar size={14} /> {formattedDate}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--accent-neon)', whiteSpace: 'nowrap' }}>
            <Tag size={14} /> {category}
          </span>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem', flexShrink: 0 }}>
        <span style={{ fontSize: '1.25rem', fontWeight: 'bold', color: 'var(--text-main)', whiteSpace: 'nowrap' }} title={`R$ ${amount.toFixed(2)}`}>
          R$ {amount.toFixed(2).replace('.', ',')}
        </span>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px', color: 'rgba(148, 163, 184, 0.7)', marginRight: '8px', whiteSpace: 'nowrap' }}>
            <Sparkles size={12} /> {(confidence * 100).toFixed(0)}%
          </span>
          <button onClick={() => onEdit(id)} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', flexShrink: 0 }} title="Editar">
            <Pencil size={16} />
          </button>
          <button onClick={() => onDelete(id)} style={{ background: 'transparent', border: 'none', color: 'var(--danger)', cursor: 'pointer', flexShrink: 0 }} title="Excluir">
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}

