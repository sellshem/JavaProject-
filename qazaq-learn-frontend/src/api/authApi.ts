import axiosInstance from './axios';
import { AuthResponse } from '../types';

export const authApi = {
  register: async (fullName: string, email: string, password: string, role: string) => {
    const response = await axiosInstance.post<AuthResponse>('/api/auth/register', {
      fullName,
      email,
      password,
      role,
    });
    return response.data;
  },

  login: async (email: string, password: string) => {
    const response = await axiosInstance.post<AuthResponse>('/api/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  refresh: async (refreshToken: string) => {
    const response = await axiosInstance.post<AuthResponse>('/api/auth/refresh', {
      refreshToken,
    });
    return response.data;
  },
};
