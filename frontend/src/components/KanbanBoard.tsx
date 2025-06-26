import React, { useState, useEffect } from 'react';
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { Task, TaskStatus, TaskCreateDto, TaskUpdateDto } from '../types/Task';
import { taskApi } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import TaskCard from './TaskCard';
import TaskForm from './TaskForm';
import DroppableColumn from './DroppableColumn';
import './KanbanBoard.css';

const KanbanBoard: React.FC = () => {
  const { addNotification } = useNotification();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | undefined>();
  const [activeTask, setActiveTask] = useState<Task | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

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
      addNotification(`タスク「${newTask.title}」を作成しました`, 'success');
    } catch (err) {
      setError('Failed to create task');
      console.error('Error creating task:', err);
    }
  };

  const handleUpdateTask = async (id: number, updates: TaskUpdateDto, showNotification = false) => {
    try {
      const updatedTask = await taskApi.updateTask(id, updates);
      setTasks(prev => prev.map(task => task.id === id ? updatedTask : task));
      setError(null);
      
      if (showNotification) {
        addNotification(`タスク「${updatedTask.title}」を更新しました`, 'success');
      }
    } catch (err) {
      setError('Failed to update task');
      console.error('Error updating task:', err);
    }
  };

  const handleDeleteTask = async (id: number) => {
    const taskToDelete = tasks.find(task => task.id === id);
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await taskApi.deleteTask(id);
        setTasks(prev => prev.filter(task => task.id !== id));
        setError(null);
        if (taskToDelete) {
          addNotification(`タスク「${taskToDelete.title}」を削除しました`, 'success');
        }
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
      await handleUpdateTask(editingTask.id, taskData, true);
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
      case TaskStatus.IN_REVIEW:
        return 'In Review';
      case TaskStatus.DONE:
        return 'Done';
      default:
        return status;
    }
  };

  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const task = tasks.find(task => task.id.toString() === active.id);
    setActiveTask(task || null);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    
    if (!over) {
      setActiveTask(null);
      return;
    }

    const taskId = parseInt(active.id.toString());
    const newStatus = over.id as TaskStatus;
    
    const task = tasks.find(t => t.id === taskId);
    if (task && task.status !== newStatus) {
      await handleUpdateTask(taskId, { status: newStatus });
    }
    
    setActiveTask(null);
  };

  if (loading) {
    return <div className="loading">Loading tasks...</div>;
  }

  return (
    <DndContext
      sensors={sensors}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className='kanban-board'>
        <header className='kanban-header'>
          <div className='header-left'></div>
          <div className='header-right'>
            <button
              onClick={() => setShowForm(true)}
              className='btn-add-task'
            >
              Add New Task
            </button>
          </div>
        </header>

        {error && (
          <div className='error-message'>
            {error}
            <button onClick={() => setError(null)} className='error-close'>
              ×
            </button>
          </div>
        )}

        <div className='kanban-columns'>
          {Object.values(TaskStatus).map(status => (
            <DroppableColumn
              key={status}
              status={status}
              title={getColumnTitle(status)}
              tasks={getTasksByStatus(status)}
              onUpdateTask={handleUpdateTask}
              onDeleteTask={handleDeleteTask}
              onEditTask={handleEditTask}
              activeTask={activeTask}
            />
          ))}
        </div>

        <DragOverlay>
          {activeTask ? (
            <TaskCard
              task={activeTask}
              onUpdateTask={() => {}}
              onDeleteTask={() => {}}
              onEditTask={() => {}}
              isDragging={true}
              isOverlay={true}
            />
          ) : null}
        </DragOverlay>
        {showForm && (
          <TaskForm
            task={editingTask}
            modeTitle={editingTask ? 'Edit Task' : 'Create New Task'}
            onSubmit={editingTask ? handleEditSubmit : handleCreateTask}
            onCancel={handleFormCancel}
          />
        )}
      </div>
    </DndContext>
  );
};

export default KanbanBoard;