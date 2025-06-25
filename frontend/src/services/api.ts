import axios from 'axios';
import { Task, TaskCreateDto, TaskUpdateDto, TaskStatus } from '../types/Task';

// ログアウト処理のコールバック関数を保存する変数
let logoutCallback: (() => void) | null = null;

// エラー通知のコールバック関数を保存する変数
let errorNotificationCallback: ((message: string) => void) | null = null;

const API_BASE_URL = `${
  process.env.API_BASE_URL || 'http://localhost:8080'
}/api`;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor to handle authentication errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // JWTトークンの期限切れまたは認証エラー
      const errorMessage = error.response?.status === 403 
        ? 'セッションの有効期限が切れました。再度ログインしてください。'
        : '認証に失敗しました。再度ログインしてください。';
      
      // エラー通知を表示
      if (errorNotificationCallback) {
        errorNotificationCallback(errorMessage);
      }
      
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // ログアウトコールバックが設定されている場合は実行
      if (logoutCallback) {
        logoutCallback();
      } else {
        // フォールバック: ログインページにリダイレクト
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// ログアウトコールバックを設定する関数
export const setLogoutCallback = (callback: () => void) => {
  logoutCallback = callback;
};

// エラー通知コールバックを設定する関数
export const setErrorNotificationCallback = (callback: (message: string) => void) => {
  errorNotificationCallback = callback;
};

export const taskApi = {
  getAllTasks: async (): Promise<Task[]> => {
    const response = await api.get<Task[]>('/tasks');
    return response.data;
  },

  getTaskById: async (id: number): Promise<Task> => {
    const response = await api.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  createTask: async (task: TaskCreateDto): Promise<Task> => {
    const response = await api.post<Task>('/tasks', task);
    return response.data;
  },

  updateTask: async (id: number, task: TaskUpdateDto): Promise<Task> => {
    const response = await api.put<Task>(`/tasks/${id}`, task);
    return response.data;
  },

  deleteTask: async (id: number): Promise<void> => {
    await api.delete(`/tasks/${id}`);
  },

  getTasksByStatus: async (status: TaskStatus): Promise<Task[]> => {
    const response = await api.get<Task[]>(`/tasks/status/${status}`);
    return response.data;
  },
};

export default api;