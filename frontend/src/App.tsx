import React, { useState, useEffect } from 'react';
import KanbanBoard from './components/KanbanBoard';
import Login from './components/Login';
import Register from './components/Register';
import { User } from './types/Task';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showRegister, setShowRegister] = useState(false);

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

  const handleLogin = (token: string, user: User) => {
    setUser(user);
    setIsAuthenticated(true);
    setShowRegister(false);
  };

  const handleRegister = (token: string, user: User) => {
    setUser(user);
    setIsAuthenticated(true);
    setShowRegister(false);
  };

  const handleShowRegister = () => {
    setShowRegister(true);
  };

  const handleBackToLogin = () => {
    setShowRegister(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setIsAuthenticated(false);
  };

  if (isLoading) {
    return <div className="loading">読み込み中...</div>;
  }

  return (
    <div className="App">
      {isAuthenticated ? (
        <>
          <header className="app-header">
            <h1>Kanban Board</h1>
            <div className="user-info">
              <span>{user?.name}さん</span>
              <button onClick={handleLogout} className="logout-btn">ログアウト</button>
            </div>
          </header>
          <KanbanBoard />
        </>
      ) : showRegister ? (
        <Register onRegister={handleRegister} onBackToLogin={handleBackToLogin} />
      ) : (
        <Login onLogin={handleLogin} onShowRegister={handleShowRegister} />
      )}
    </div>
  );
}

export default App;
