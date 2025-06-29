import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import { Task, TaskCreateDto } from '../types/Task';
import './TaskForm.css';
import '../styles/highlight.css';

interface TaskFormProps {
  task?: Task;
  modeTitle: string;
  onSubmit: (taskData: TaskCreateDto) => void;
  onCancel: () => void;
}

const TaskForm: React.FC<TaskFormProps> = ({
  task,
  modeTitle,
  onSubmit,
  onCancel,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [showPreview, setShowPreview] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({
    title: '',
    description: '',
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
      description: '',
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
      description: '',
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
    <div className='task-form-overlay' onClick={handleOverlayClick}>
      <div className='task-form-modal'>
        <form onSubmit={handleSubmit} className='task-form' noValidate>
          <h2>{modeTitle}</h2>
          <div className='form-group'>
            <label htmlFor='title'>Title *</label>
            <input
              type='text'
              id='title'
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder='Enter task title'
              required
            />
            {fieldErrors.title && (
              <div className='field-error-message'>{fieldErrors.title}</div>
            )}
          </div>

          <div className='form-group'>
            <div className='description-header'>
              <label htmlFor='description'>Description (Markdown supported)</label>
              <div className='description-tabs'>
                <button 
                  type='button' 
                  className={`tab-button ${!showPreview ? 'active' : ''}`}
                  onClick={() => setShowPreview(false)}
                >
                  Write
                </button>
                <button 
                  type='button' 
                  className={`tab-button ${showPreview ? 'active' : ''}`}
                  onClick={() => setShowPreview(true)}
                  disabled={!description.trim()}
                >
                  Preview
                </button>
              </div>
            </div>
            
            {!showPreview ? (
              <textarea
                id='description'
                value={description}
                onChange={e => setDescription(e.target.value)}
                placeholder='Enter task description using Markdown syntax (optional)&#10;&#10;Examples:&#10;**Bold text**&#10;*Italic text*&#10;`Code`&#10;- List item&#10;[Link](https://example.com)&#10;&#10;```javascript&#10;console.log("Code block");&#10;```'
                rows={6}
                className='markdown-textarea'
              />
            ) : (
              <div className='markdown-preview'>
                {description.trim() ? (
                  <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
                      a: ({href, children}) => (
                        <a href={href} target="_blank" rel="noopener noreferrer">
                          {children}
                        </a>
                      ),
                    }}
                  >
                    {description}
                  </ReactMarkdown>
                ) : (
                  <div className='preview-placeholder'>
                    Nothing to preview
                  </div>
                )}
              </div>
            )}
            
            {fieldErrors.description && (
              <div className='field-error-message'>
                {fieldErrors.description}
              </div>
            )}
            
            <div className='markdown-help'>
              <small>
                You can use Markdown syntax: **bold**, *italic*, `code`, [links](url), lists, and code blocks
              </small>
            </div>
          </div>

          <div className='form-actions'>
            <button type='button' onClick={onCancel} className='btn-cancel'>
              Cancel
            </button>
            <button type='submit' className='btn-submit'>
              {task ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskForm;