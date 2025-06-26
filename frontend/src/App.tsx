import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import AppRouter from './components/AppRouter';
import NotificationContainer from './components/NotificationContainer';
import { NotificationProvider, useNotification } from './contexts/NotificationContext';
import { User } from './types/Task';
import { setLogoutCallback, setErrorNotificationCallback } from './services/api';
import './App.css';

const AppWithNotification: React.FC = () => {
  const navigate = useNavigate();
  const { addNotification } = useNotification();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser && storedUser !== 'undefined') {
      try {
        setUser(JSON.parse(storedUser));
        setIsAuthenticated(true);
      } catch (error) {
        console.error('Failed to parse stored user data:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setIsLoading(false);
  }, []);

  const handleLogin = (user: User) => {
    setUser(user);
    setIsAuthenticated(true);
  };

  const handleRegister = (user: User) => {
    setUser(user);
    setIsAuthenticated(true);
  };

  const handleLogout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setIsAuthenticated(false);
    navigate('/login');
  }, [navigate]);

  const handleUserUpdate = (updatedUser: User) => {
    setUser(updatedUser);
  };

  // APIインターセプターにコールバックを設定
  useEffect(() => {
    setLogoutCallback(handleLogout);
    setErrorNotificationCallback((message: string) => {
      addNotification(message, 'error');
    });
  }, [handleLogout, addNotification]);

  if (isLoading) {
    return <div className="loading">読み込み中...</div>;
  }

  return (
    <div className='App'>
      <AppRouter
        isAuthenticated={isAuthenticated}
        user={user}
        onLogin={handleLogin}
        onRegister={handleRegister}
        onLogout={handleLogout}
        onUserUpdate={handleUserUpdate}
      />
      <NotificationContainer />
    </div>
  );
};

function App() {
  return (
    <NotificationProvider>
      <AppWithNotification />
    </NotificationProvider>
  );
}

export default App;
