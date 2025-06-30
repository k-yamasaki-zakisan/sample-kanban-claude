import React, { useState, useEffect } from 'react';
import { Task, TaskCreateDto } from '../types/Task';
import MarkdownEditor from './MarkdownEditor';
import { useNotification } from '../contexts/NotificationContext';
import './TaskForm.css';

interface TaskFormProps {
  task?: Task;
  modeTitle: string;
  onSubmit: (taskData: TaskCreateDto) => void | Promise<void>;
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
  const [fieldErrors, setFieldErrors] = useState({
    title: '',
    description: '',
  });
  const { addNotification } = useNotification();

  useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description || '');
    } else {
      setTitle('');
      setDescription('');
      // 新規作成時はフォームを閉じる際に一時画像を削除
    }
  }, [task]);

  useEffect(() => {
    // コンポーネントがアンマウントされる際に一時画像を削除
    return () => {
      if (!task) {
        // 一時的に無効化 - 認証問題のため
        // console.log('TaskForm unmounting, attempting to delete temporary images');
        // imageApi.deleteTemporaryImages().catch(error => {
        //   console.error('Failed to delete temporary images:', error);
        //   // 403エラーの場合は認証問題なので特別な処理はしない
        //   if (error.response?.status === 403) {
        //     console.log('403 error during cleanup - likely authentication issue');
        //   }
        // });
      }
    };
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({
      title: '',
      description: '',
    });

    if (!validateForm()) {
      return;
    }

    try {
      const result = onSubmit({
        title: title.trim(),
        description: description.trim() || undefined,
      });
      
      // Promiseかどうかをチェック
      if (result && typeof result.then === 'function') {
        await result;
      }
      
      setTitle('');
      setDescription('');
      
      // タスク作成/更新成功時に通知
      addNotification(
        task ? 'タスクを更新しました' : 'タスクを作成しました',
        'success'
      );
    } catch (error) {
      console.error('Error submitting task:', error);
      addNotification(
        task ? 'タスクの更新に失敗しました' : 'タスクの作成に失敗しました',
        'error'
      );
    }
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      handleCancel();
    }
  };

  const handleCancel = async () => {
    // 新規作成時のみ一時画像を削除
    if (!task) {
      // 一時的に無効化 - 認証問題のため
      // try {
      //   await imageApi.deleteTemporaryImages();
      // } catch (error) {
      //   console.error('Failed to delete temporary images:', error);
      // }
    }
    onCancel();
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
            <label htmlFor='description'>Description</label>
            <MarkdownEditor
              value={description}
              onChange={setDescription}
              placeholder='Enter task description (optional)'
              rows={6}
            />
            {fieldErrors.description && (
              <div className='field-error-message'>
                {fieldErrors.description}
              </div>
            )}
          </div>

          <div className='form-actions'>
            <button type='button' onClick={handleCancel} className='btn-cancel'>
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