import { UserRole } from '../types';

export const authUtils = {
  saveAuthData: (accessToken: string, refreshToken: string, userId: string, email: string, role: UserRole) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userId', userId);
    localStorage.setItem('email', email);
    localStorage.setItem('role', role);
  },

  getAuthData: () => ({
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    userId: localStorage.getItem('userId'),
    email: localStorage.getItem('email'),
    role: localStorage.getItem('role') as UserRole | null,
  }),

  clearAuthData: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
  },

  isAuthenticated: () => !!localStorage.getItem('accessToken'),

  getRole: () => localStorage.getItem('role') as UserRole | null,

  getRefreshToken: () => localStorage.getItem('refreshToken'),
};

export const errorUtils = {
  getErrorMessage: (error: any): string => {
    if (error.response?.status === 401) {
      return 'Логин немесе пароль қатесі';
    }
    if (error.response?.status === 403) {
      return 'Құқығыңыз жоқ';
    }
    if (error.response?.status === 429) {
      return 'Too many requests. Please try again later.';
    }
    if (error.response?.status === 500) {
      return 'Сервер қатесі';
    }
    if (error.response?.data?.message) {
      return error.response.data.message;
    }
    return error.message || 'Қате орын алды';
  },
};
