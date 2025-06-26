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
  IN_REVIEW = 'IN_REVIEW',
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

export interface User {
  id: number;
  name: string;
  email: string;
  lastLogin: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateDto {
  name?: string;
  email?: string;
}

export interface UserUpdateResponse {
  user: User;
  token: string;
}