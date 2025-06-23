import React from 'react';
import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Task, TaskStatus, TaskUpdateDto } from '../types/Task';
import TaskCard from './TaskCard';

interface DroppableColumnProps {
  status: TaskStatus;
  title: string;
  tasks: Task[];
  onUpdateTask: (id: number, updates: TaskUpdateDto) => void;
  onDeleteTask: (id: number) => void;
  onEditTask: (task: Task) => void;
  activeTask: Task | null;
}

const DroppableColumn: React.FC<DroppableColumnProps> = ({
  status,
  title,
  tasks,
  onUpdateTask,
  onDeleteTask,
  onEditTask,
  activeTask,
}) => {
  const { isOver, setNodeRef } = useDroppable({
    id: status,
  });

  return (
    <div className="kanban-column" data-status={status}>
      <div className="column-header">
        <h2>{title}</h2>
        <span className="task-count">{tasks.length}</span>
      </div>
      <SortableContext 
        items={tasks.map(task => task.id.toString())}
        strategy={verticalListSortingStrategy}
      >
        <div 
          ref={setNodeRef}
          className={`column-content droppable-area ${isOver ? 'drag-over' : ''}`}
          data-droppable-id={status}
        >
          {tasks.map(task => (
            <TaskCard
              key={task.id}
              task={task}
              onUpdateTask={onUpdateTask}
              onDeleteTask={onDeleteTask}
              onEditTask={onEditTask}
              isDragging={activeTask?.id === task.id}
            />
          ))}
          {tasks.length === 0 && (
            <div className={`empty-column ${isOver ? 'drag-over' : ''}`}>
              Drop tasks here
            </div>
          )}
        </div>
      </SortableContext>
    </div>
  );
};

export default DroppableColumn;