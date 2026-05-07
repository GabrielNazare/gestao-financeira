import React, { useState } from 'react';
import { GoogleLogin } from '@react-oauth/google';
import { Sparkles, BarChart3, Languages, Loader2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { authService } from '../services/authService';
import './LandingPage.css';

interface LandingPageProps {
  onLoginSuccess: (token: string) => void;
}

export function LandingPage({ onLoginSuccess }: LandingPageProps) {
  const { t, i18n } = useTranslation();
  const [isLoginMode, setIsLoginMode] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const toggleLanguage = () => {
    const newLang = i18n.language === 'pt-BR' ? 'en' : 'pt-BR';
    i18n.changeLanguage(newLang);
  };

  const handleGoogleSuccess = async (response: any) => {
    try {
      setIsLoading(true);
      const data = await authService.googleLogin(response.credential);
      onLoginSuccess(data.token);
    } catch (err) {
      alert(t('landing.auth_error'));
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const payload = isLoginMode ? { email, password } : { email, password, name };
      const data = isLoginMode 
        ? await authService.login(payload) 
        : await authService.register(payload);
      onLoginSuccess(data.token);
    } catch (err: any) {
      alert(err.response?.data?.error || t('landing.auth_error'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="landing-container">
      <nav className="navbar glass-panel">
        <div className="logo">
          <Sparkles className="icon-neon" />
          <span className="text-gradient font-bold">FinAI</span>
        </div>
        <div className="auth-btn-wrapper" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button
            onClick={toggleLanguage}
            className="btn-primary"
            style={{ background: 'transparent', border: '1px solid var(--border-glass)', boxShadow: 'none', padding: '0.5rem 0.8rem', display: 'flex', alignItems: 'center', gap: '6px' }}
            title="Switch Language"
          >
            <Languages size={18} />
            <span style={{ fontWeight: 'bold', fontSize: '0.8rem' }}>{i18n.language.toUpperCase()}</span>
          </button>
        </div>
      </nav>

      <main className="hero-section" style={{ display: 'flex', gap: '4rem', padding: '0 2rem' }}>
        <div className="hero-content" style={{ flex: 1, maxWidth: '600px', textAlign: 'left' }}>
          <h1 className="hero-title" style={{ fontSize: '3.5rem', marginBottom: '1.5rem' }}>
            {t('landing.title')} <br />
            <span className="text-gradient">{t('landing.subtitle')}</span>
          </h1>
          <p className="hero-subtitle" style={{ marginBottom: '3rem' }}>
            {t('landing.description')}
          </p>

          <div className="features-grid">
            <div className="feature-card glass-panel">
              <Sparkles className="feature-icon" />
              <h3>{t('landing.feature_ai_title')}</h3>
              <p>{t('landing.feature_ai_desc')}</p>
            </div>
            <div className="feature-card glass-panel">
              <BarChart3 className="feature-icon" />
              <h3>{t('dashboard.table.category')}</h3>
              <p>{t('analytics.category_chart')}</p>
            </div>
          </div>
        </div>

        <div className="auth-panel glass-panel" style={{ width: '400px', padding: '2rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <h2 style={{ color: 'var(--text-main)', textAlign: 'center', fontSize: '1.5rem' }}>
            {isLoginMode ? t('landing.login_button') : t('landing.register_button')}
          </h2>

          <form onSubmit={handleAuthSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {!isLoginMode && (
              <input
                type="text"
                className="input-glass"
                placeholder={t('landing.name_placeholder')}
                value={name}
                onChange={e => setName(e.target.value)}
                required
              />
            )}
            <input
              type="email"
              className="input-glass"
              placeholder={t('landing.email_placeholder')}
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
            <input
              type="password"
              className="input-glass"
              placeholder={t('landing.password_placeholder')}
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
            <button type="submit" className="btn-primary" disabled={isLoading}>
              {isLoading ? <Loader2 className="spinner" size={18} /> : null}
              {isLoginMode ? t('landing.login_button') : t('landing.register_button')}
            </button>
          </form>

          <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
            {isLoginMode ? t('landing.no_account') : t('landing.has_account')}
            <button
              onClick={() => setIsLoginMode(!isLoginMode)}
              style={{ background: 'none', border: 'none', color: 'var(--accent-neon)', marginLeft: '0.5rem', cursor: 'pointer', fontWeight: 'bold' }}>
              {isLoginMode ? t('landing.register_link') : t('landing.login_link')}
            </button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            <div style={{ flex: 1, height: '1px', background: 'var(--border-glass)' }}></div>
            <span style={{ padding: '0 1rem' }}>OU</span>
            <div style={{ flex: 1, height: '1px', background: 'var(--border-glass)' }}></div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => alert('Google Login Failed')}
              theme="filled_black"
              shape="pill"
              text={isLoginMode ? "signin_with" : "signup_with"}
            />
          </div>
        </div>
      </main>
    </div>
  );
}
