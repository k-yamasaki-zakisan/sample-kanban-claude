export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  createdAt: string;
  updatedAt: string;
}

export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE'
}

export interface TaskCreateDto {
  title: string;
  description?: string;
}

export interface TaskUpdateDto {
  title?: string;
  description?: string;
  status?: TaskStatus;
}