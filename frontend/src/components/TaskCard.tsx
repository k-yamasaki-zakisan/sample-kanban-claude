import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Task, TaskStatus } from '../types/Task';
import './TaskCard.css';

interface TaskCardProps {
  task: Task;
  onUpdateTask: (id: number, updates: { status?: TaskStatus }) => void;
  onDeleteTask: (id: number) => void;
  onEditTask: (task: Task) => void;
  isDragging?: boolean;
  isOverlay?: boolean;
}

const TaskCard: React.FC<TaskCardProps> = ({ 
  task, 
  onUpdateTask, 
  onDeleteTask, 
  onEditTask, 
  isDragging = false,
  isOverlay = false
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging: isSortableDragging,
  } = useSortable({ id: task.id.toString() });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const handleStatusChange = (newStatus: TaskStatus) => {
    onUpdateTask(task.id, { status: newStatus });
  };

  const getStatusColor = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.TODO:
        return '#e74c3c';
      case TaskStatus.IN_PROGRESS:
        return '#f39c12';
      case TaskStatus.IN_REVIEW:
        return '#9b59b6';
      case TaskStatus.DONE:
        return '#27ae60';
      default:
        return '#95a5a6';
    }
  };

  const truncateText = (text: string, maxLength = 100): string => {
    if (!text) {
      return '';
    }
    
    if (text.length <= maxLength) {
      return text;
    }
    
    return `${text.substring(0, maxLength)}...`;
  };

  return (
    <div 
      ref={setNodeRef}
      style={style}
      className={`task-card ${isDragging || isSortableDragging ? 'dragging' : ''} ${isOverlay ? 'overlay' : ''}`}
      {...attributes}
      {...listeners}
    >
      <div className="task-header">
        <h3 className="task-title">{task.title}</h3>
        <div className="task-actions">
          <button onClick={() => onEditTask(task)} className="btn-edit">
            Edit
          </button>
          <button onClick={() => onDeleteTask(task.id)} className="btn-delete">
            Delete
          </button>
        </div>
      </div>
      {task.description && (
        <div className="task-description">
          <div className="markdown-content-card">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {truncateText(task.description)}
            </ReactMarkdown>
          </div>
        </div>
      )}
      
      {task.images && task.images.length > 0 && (
        <div className="task-images">
          <div className="task-images-grid">
            {task.images.slice(0, 4).map((image, index) => (
              <div key={image.id} className="task-image-thumbnail">
                <img
                  src={image.imageUrl}
                  alt={image.originalFilename}
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.style.display = 'none';
                  }}
                />
              </div>
            ))}
            {task.images.length > 4 && (
              <div className="task-images-more">
                +{task.images.length - 4}
              </div>
            )}
          </div>
        </div>
      )}
      <div className="task-footer">
        <select
          value={task.status}
          onChange={(e) => handleStatusChange(e.target.value as TaskStatus)}
          className="status-select"
          style={{ borderColor: getStatusColor(task.status) }}
        >
          <option value={TaskStatus.TODO}>To Do</option>
          <option value={TaskStatus.IN_PROGRESS}>In Progress</option>
          <option value={TaskStatus.IN_REVIEW}>In Review</option>
          <option value={TaskStatus.DONE}>Done</option>
        </select>
        <span className="task-date">
          {new Date(task.createdAt).toLocaleDateString()}
        </span>
      </div>
    </div>
  );
};

export default TaskCard;