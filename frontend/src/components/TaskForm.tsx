import React, { useState, useEffect } from 'react';
import { Task, TaskCreateDto } from '../types/Task';
import './TaskForm.css';

interface TaskFormProps {
  task?: Task;
  onSubmit: (taskData: TaskCreateDto) => void;
  onCancel: () => void;
}

const TaskForm: React.FC<TaskFormProps> = ({ task, onSubmit, onCancel }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [fieldErrors, setFieldErrors] = useState({
    title: '',
    description: ''
  });

  useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description || '');
    }
  }, [task]);

  const validateForm = () => {
    const errors = {
      title: '',
      description: ''
    };

    if (!title.trim()) {
      errors.title = 'タイトルは必須です。';
    } else if (title.trim().length > 100) {
      errors.title = 'タイトルは100文字以内で入力してください。';
    }

    if (description.trim().length > 500) {
      errors.description = '説明は500文字以内で入力してください。';
    }

    setFieldErrors(errors);
    return !errors.title && !errors.description;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({
      title: '',
      description: ''
    });

    if (!validateForm()) {
      return;
    }

    onSubmit({
      title: title.trim(),
      description: description.trim() || undefined,
    });
    setTitle('');
    setDescription('');
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onCancel();
    }
  };

  return (
    <div className="task-form-overlay" onClick={handleOverlayClick}>
      <div className="task-form-modal">
        <form onSubmit={handleSubmit} className="task-form" noValidate>
          <h2>{task ? 'Edit Task' : 'Create New Task'}</h2>
          
          <div className="form-group">
            <label htmlFor="title">Title *</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Enter task title"
              required
            />
            {fieldErrors.title && <div className="field-error-message">{fieldErrors.title}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Enter task description (optional)"
              rows={4}
            />
            {fieldErrors.description && <div className="field-error-message">{fieldErrors.description}</div>}
          </div>

          <div className="form-actions">
            <button type="button" onClick={onCancel} className="btn-cancel">
              Cancel
            </button>
            <button type="submit" className="btn-submit">
              {task ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskForm;