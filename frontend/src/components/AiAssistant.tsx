import React, { useState } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import { Send, Bot, Loader2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import './AiAssistant.css';

export function AiAssistant() {
  const { t } = useTranslation();
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleAsk = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) return;

    setIsLoading(true);
    setAnswer(null);

    try {
      const response = await axios.post('http://localhost:8080/api/insights/ask', {
        question: question
      });
      setAnswer(response.data.answer);
    } catch (error) {
      setAnswer(t('ai_assistant.error'));
    } finally {
      setIsLoading(false);
      setQuestion('');
    }
  };

  return (
    <div className="ai-assistant-container glass-panel">
      <div className="ai-header">
        <Bot className="icon-neon" size={24} />
        <h3>{t('ai_assistant.title')}</h3>
      </div>

      <div className="ai-content">
        {!answer && !isLoading && (
          <div className="ai-empty-state text-muted text-sm">
            {t('ai_assistant.welcome')}
          </div>
        )}

        {isLoading && (
          <div className="ai-loading">
            <Loader2 className="spinner icon-neon" size={24} />
            <span className="text-sm text-muted">{t('ai_assistant.thinking')}</span>
          </div>
        )}

        {answer && !isLoading && (
          <div className="ai-response">
            <ReactMarkdown>{answer}</ReactMarkdown>
          </div>
        )}
      </div>

      <form onSubmit={handleAsk} className="ai-input-area">
        <input
          type="text"
          className="input-glass ai-input"
          placeholder={t('ai_assistant.placeholder')}
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          disabled={isLoading}
        />
        <button type="submit" className="ai-send-btn" disabled={isLoading || !question.trim()}>
          <Send size={18} />
        </button>
      </form>
    </div>
  );
}

