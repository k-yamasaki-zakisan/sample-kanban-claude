import React, { useState, useEffect } from 'react';
import { Task, TaskStatus, TaskCreateDto, TaskUpdateDto } from '../types/Task';
import { taskApi } from '../services/api';
import TaskCard from './TaskCard';
import TaskForm from './TaskForm';
import './KanbanBoard.css';

const KanbanBoard: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | undefined>();

  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = async () => {
    try {
      setLoading(true);
      const tasksData = await taskApi.getAllTasks();
      setTasks(tasksData);
      setError(null);
    } catch (err) {
      setError('Failed to load tasks');
      console.error('Error loading tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (taskData: TaskCreateDto) => {
    try {
      const newTask = await taskApi.createTask(taskData);
      setTasks(prev => [newTask, ...prev]);
      setShowForm(false);
      setError(null);
    } catch (err) {
      setError('Failed to create task');
      console.error('Error creating task:', err);
    }
  };

  const handleUpdateTask = async (id: number, updates: TaskUpdateDto) => {
    try {
      const updatedTask = await taskApi.updateTask(id, updates);
      setTasks(prev => prev.map(task => task.id === id ? updatedTask : task));
      setError(null);
    } catch (err) {
      setError('Failed to update task');
      console.error('Error updating task:', err);
    }
  };

  const handleDeleteTask = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await taskApi.deleteTask(id);
        setTasks(prev => prev.filter(task => task.id !== id));
        setError(null);
      } catch (err) {
        setError('Failed to delete task');
        console.error('Error deleting task:', err);
      }
    }
  };

  const handleEditTask = (task: Task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleEditSubmit = async (taskData: TaskCreateDto) => {
    if (editingTask) {
      await handleUpdateTask(editingTask.id, taskData);
      setEditingTask(undefined);
      setShowForm(false);
    }
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditingTask(undefined);
  };

  const getTasksByStatus = (status: TaskStatus) => {
    return tasks.filter(task => task.status === status);
  };

  const getColumnTitle = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.TODO:
        return 'To Do';
      case TaskStatus.IN_PROGRESS:
        return 'In Progress';
      case TaskStatus.DONE:
        return 'Done';
      default:
        return status;
    }
  };

  if (loading) {
    return <div className="loading">Loading tasks...</div>;
  }

  return (
    <div className="kanban-board">
      <header className="kanban-header">
        <h1>Kanban Board</h1>
        <button onClick={() => setShowForm(true)} className="btn-add-task">
          Add New Task
        </button>
      </header>

      {error && (
        <div className="error-message">
          {error}
          <button onClick={() => setError(null)} className="error-close">×</button>
        </div>
      )}

      <div className="kanban-columns">
        {Object.values(TaskStatus).map(status => (
          <div key={status} className="kanban-column">
            <div className="column-header">
              <h2>{getColumnTitle(status)}</h2>
              <span className="task-count">{getTasksByStatus(status).length}</span>
            </div>
            <div className="column-content">
              {getTasksByStatus(status).map(task => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onUpdateTask={handleUpdateTask}
                  onDeleteTask={handleDeleteTask}
                  onEditTask={handleEditTask}
                />
              ))}
              {getTasksByStatus(status).length === 0 && (
                <div className="empty-column">No tasks</div>
              )}
            </div>
          </div>
        ))}
      </div>

      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleEditSubmit : handleCreateTask}
          onCancel={handleFormCancel}
        />
      )}
    </div>
  );
};

export default KanbanBoard;