import { useState, useEffect } from 'react';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { LandingPage } from './pages/LandingPage';
import { Dashboard } from './pages/Dashboard';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const handleLoginSuccess = (appToken: string) => {
    localStorage.setItem('token', appToken);
    setToken(appToken);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
  };

  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      {token ? (
        <Dashboard token={token} onLogout={handleLogout} />
      ) : (
        <LandingPage onLoginSuccess={handleLoginSuccess} />
      )}
    </GoogleOAuthProvider>
  );
}

export default App;

