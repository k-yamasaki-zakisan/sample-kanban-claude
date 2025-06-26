import React, { createContext, useContext, useState, ReactNode, useCallback, useMemo, useRef } from 'react';

interface Notification {
  id: string;
  message: string;
  type: 'info' | 'error' | 'success';
}

interface NotificationActionsContextType {
  addNotification: (message: string, type?: 'info' | 'error' | 'success') => void;
}

interface NotificationStateContextType {
  notifications: Notification[];
  removeNotification: (id: string) => void;
}

const NotificationActionsContext = createContext<NotificationActionsContextType | undefined>(undefined);
const NotificationStateContext = createContext<NotificationStateContextType | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationActionsContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

export const useNotificationState = () => {
  const context = useContext(NotificationStateContext);
  if (!context) {
    throw new Error('useNotificationState must be used within a NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const removeTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());

  const removeNotification = useCallback((id: string) => {
    const timeout = removeTimeoutsRef.current.get(id);
    if (timeout) {
      clearTimeout(timeout);
      removeTimeoutsRef.current.delete(id);
    }
    setNotifications(prev => prev.filter(notification => notification.id !== id));
  }, []);

  const addNotification = useCallback((message: string, type: 'info' | 'error' | 'success' = 'info') => {
    const id = Math.random().toString(36).substring(2, 11);
    const notification: Notification = { id, message, type };
    
    setNotifications(prev => [...prev, notification]);
    
    // 5秒後に自動削除
    const timeout = setTimeout(() => {
      removeNotification(id);
    }, 5000);
    
    removeTimeoutsRef.current.set(id, timeout);
  }, [removeNotification]);

  // アクションのContextは通知状態の変更に影響されない
  const actionsValue = useMemo(() => ({
    addNotification
  }), [addNotification]);

  // 状態のContextは通知の表示コンポーネントのみが使用
  const stateValue = useMemo(() => ({
    notifications,
    removeNotification
  }), [notifications, removeNotification]);

  return (
    <NotificationActionsContext.Provider value={actionsValue}>
      <NotificationStateContext.Provider value={stateValue}>
        {children}
      </NotificationStateContext.Provider>
    </NotificationActionsContext.Provider>
  );
};