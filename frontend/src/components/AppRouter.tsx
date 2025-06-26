import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { User } from '../types/Task';
import Layout from './Layout';
import Login from './Login';
import Register from './Register';
import KanbanBoard from './KanbanBoard';
import Profile from './Profile';
import Dashboard from './Dashboard';

interface AppRouterProps {
  isAuthenticated: boolean;
  user: User | null;
  onLogin: (user: User) => void;
  onRegister: (user: User) => void;
  onLogout: () => void;
  onUserUpdate: (user: User) => void;
}

const AppRouter: React.FC<AppRouterProps> = ({
  isAuthenticated,
  user,
  onLogin,
  onRegister,
  onLogout,
  onUserUpdate,
}) => {
  const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
  };

  const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return !isAuthenticated ? <>{children}</> : <Navigate to="/board" replace />;
  };

  return (
    <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <Login onLogin={onLogin} />
            </PublicRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <Register onRegister={onRegister} />
            </PublicRoute>
          }
        />

        {/* Protected Routes */}
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <Layout user={user} onLogout={onLogout}>
                <Routes>
                  <Route path="/board" element={<KanbanBoard />} />
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/profile" element={<Profile user={user} onUserUpdate={onUserUpdate} />} />
                  <Route path="/" element={<Navigate to="/board" replace />} />
                  <Route path="*" element={<Navigate to="/board" replace />} />
                </Routes>
              </Layout>
            </ProtectedRoute>
          }
        />
      </Routes>
  );
};

export default AppRouter;