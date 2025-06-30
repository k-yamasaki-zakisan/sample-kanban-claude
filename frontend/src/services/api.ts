import axios from 'axios';
import { Task, TaskCreateDto, TaskUpdateDto, TaskStatus, User, UserUpdateDto, UserUpdateResponse, TaskImage } from '../types/Task';

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
  console.log('API Request:', config.method?.toUpperCase(), config.url);
  console.log('Token exists:', !!token);
  
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('Authorization header set:', `${config.headers.Authorization?.substring(0, 20)}...`);
  } else {
    console.log('No token found in localStorage');
  }

  // FormDataの場合はContent-Typeを削除してブラウザに自動設定させる
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
    console.log('FormData detected, Content-Type header removed');
  }
  
  return config;
});

// Interceptor to handle authentication errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
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

export const userApi = {
  updateUser: async (updateData: UserUpdateDto): Promise<User> => {
    const response = await api.put<UserUpdateResponse>('/auth/me', updateData);
    
    // 新しいJWTトークンをlocalStorageに保存
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      
      // 次のリクエストで新しいトークンが使用されるように、
      // axiosのデフォルトヘッダーを更新
      api.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
    }
    
    return response.data.user;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/auth/me');
    return response.data;
  },
};

// Image API
export const imageApi = {
  uploadImage: async (file: File): Promise<TaskImage> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post<TaskImage>('/images/upload', formData);
    return response.data;
  },

  getUserImages: async (onlyTemporary = false): Promise<TaskImage[]> => {
    const response = await api.get<TaskImage[]>(`/images/user?temporary=${onlyTemporary}`);
    return response.data;
  },

  deleteImage: async (imageId: number): Promise<void> => {
    await api.delete(`/images/${imageId}`);
  },

  deleteTemporaryImages: async (): Promise<void> => {
    await api.delete('/images/temporary');
  },

  getImageUrl: (imageId: number): string => {
    return `${api.defaults.baseURL}/images/${imageId}`;
  },

  // Legacy functions for backward compatibility
  uploadTaskImage: async (taskId: number, file: File, uploadOrder = 0): Promise<TaskImage> => {
    return imageApi.uploadImage(file);
  },

  uploadMultipleTaskImages: async (taskId: number, files: File[]): Promise<TaskImage[]> => {
    const uploadPromises = files.map(file => imageApi.uploadImage(file));
    return Promise.all(uploadPromises);
  },

  getTaskImages: async (taskId: number): Promise<TaskImage[]> => {
    return imageApi.getUserImages(false);
  },

  deleteAllTaskImages: async (taskId: number): Promise<void> => {
    return imageApi.deleteTemporaryImages();
  },
};

export default api;