import React from 'react';
import { useNotificationState } from '../contexts/NotificationContext';
import './NotificationContainer.css';

const NotificationContainer: React.FC = React.memo(() => {
  const { notifications, removeNotification } = useNotificationState();

  if (notifications.length === 0) {
    return null;
  }

  return (
    <div className="notification-container">
      {notifications.map(notification => (
        <div 
          key={notification.id} 
          className={`notification notification-${notification.type}`}
        >
          <span className="notification-message">{notification.message}</span>
          <button 
            onClick={() => removeNotification(notification.id)}
            className="notification-close"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
});

NotificationContainer.displayName = 'NotificationContainer';

export default NotificationContainer;