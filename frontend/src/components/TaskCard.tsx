import React from 'react';
import { Task, TaskStatus } from '../types/Task';
import './TaskCard.css';

interface TaskCardProps {
  task: Task;
  onUpdateTask: (id: number, updates: { status?: TaskStatus }) => void;
  onDeleteTask: (id: number) => void;
  onEditTask: (task: Task) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, onUpdateTask, onDeleteTask, onEditTask }) => {
  const handleStatusChange = (newStatus: TaskStatus) => {
    onUpdateTask(task.id, { status: newStatus });
  };

  const getStatusColor = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.TODO:
        return '#e74c3c';
      case TaskStatus.IN_PROGRESS:
        return '#f39c12';
      case TaskStatus.DONE:
        return '#27ae60';
      default:
        return '#95a5a6';
    }
  };

  return (
    <div className="task-card">
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
        <p className="task-description">{task.description}</p>
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