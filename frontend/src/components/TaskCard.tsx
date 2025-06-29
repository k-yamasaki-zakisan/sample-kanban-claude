import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import { Task, TaskStatus } from '../types/Task';
import './TaskCard.css';
import '../styles/highlight.css';

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

  const truncateMarkdown = (text: string, maxLength = 100): string => {
    if (!text) {
      return '';
    }
    // Remove markdown syntax for truncation purposes
    const plainText = text
      .replace(/#{1,6}\s+/g, '') // Remove headers
      .replace(/\*\*(.*?)\*\*/g, '$1') // Remove bold
      .replace(/\*(.*?)\*/g, '$1') // Remove italic
      .replace(/`(.*?)`/g, '$1') // Remove inline code
      .replace(/\[(.*?)\]\(.*?\)/g, '$1') // Remove links, keep text
      .replace(/```[\s\S]*?```/g, '[code block]') // Replace code blocks
      .replace(/>\s+(.*)/g, '$1') // Remove blockquotes
      .replace(/[-*+]\s+/g, '') // Remove list markers
      .replace(/\n+/g, ' ') // Replace newlines with spaces
      .trim();
    
    if (plainText.length <= maxLength) {
      return text; // Return original markdown if within limit
    }
    
    // If too long, truncate the plain text and add ellipsis
    return `${plainText.substring(0, maxLength)}...`;
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
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeHighlight]}
            components={{
              // Customize components for compact display
              h1: ({children}) => <h4>{children}</h4>,
              h2: ({children}) => <h5>{children}</h5>,
              h3: ({children}) => <h6>{children}</h6>,
              h4: ({children}) => <h6>{children}</h6>,
              h5: ({children}) => <h6>{children}</h6>,
              h6: ({children}) => <h6>{children}</h6>,
              p: ({children}) => <span className="markdown-paragraph">{children}</span>,
              code: ({children, ...props}) => {
                const inline = !props.className?.includes('language-');
                return inline ? 
                  <code className="markdown-inline-code">{children}</code> : 
                  <pre className="markdown-code-block"><code>{children}</code></pre>;
              },
              blockquote: ({children}) => <div className="markdown-blockquote">{children}</div>,
              ul: ({children}) => <ul className="markdown-list">{children}</ul>,
              ol: ({children}) => <ol className="markdown-list">{children}</ol>,
              li: ({children}) => <li className="markdown-list-item">{children}</li>,
              a: ({href, children}) => <a href={href} target="_blank" rel="noopener noreferrer" className="markdown-link">{children}</a>,
            }}
          >
            {truncateMarkdown(task.description)}
          </ReactMarkdown>
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