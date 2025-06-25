import React, { createContext, useContext, useState, ReactNode } from 'react';

interface Notification {
  id: string;
  message: string;
  type: 'info' | 'error' | 'success';
}

interface NotificationContextType {
  notifications: Notification[];
  addNotification: (message: string, type?: 'info' | 'error' | 'success') => void;
  removeNotification: (id: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const addNotification = (message: string, type: 'info' | 'error' | 'success' = 'info') => {
    const id = Math.random().toString(36).substr(2, 9);
    const notification: Notification = { id, message, type };
    
    setNotifications(prev => [...prev, notification]);
    
    // 5秒後に自動削除
    setTimeout(() => {
      removeNotification(id);
    }, 5000);
  };

  const removeNotification = (id: string) => {
    setNotifications(prev => prev.filter(notification => notification.id !== id));
  };

  return (
    <NotificationContext.Provider value={{ notifications, addNotification, removeNotification }}>
      {children}
    </NotificationContext.Provider>
  );
};