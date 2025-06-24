import React, { useState, useEffect } from 'react';
import KanbanBoard from './components/KanbanBoard';
import Login from './components/Login';
import { User } from './types/Task';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setUser(JSON.parse(storedUser));
      setIsAuthenticated(true);
    }
    setIsLoading(false);
  }, []);

  const handleLogin = (token: string, user: User) => {
    setUser(user);
    setIsAuthenticated(true);
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
      ) : (
        <Login onLogin={handleLogin} />
      )}
    </div>
  );
}

export default App;
