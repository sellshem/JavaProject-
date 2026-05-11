import axiosInstance from './axios';
import { Progress } from '../types';

export const progressApi = {
  completeLesson: async (lessonId: string) => {
    await axiosInstance.post(`/api/lessons/${lessonId}/complete`);
  },

  getMyProgress: async () => {
    const response = await axiosInstance.get<Progress[]>('/api/me/progress');
    return response.data;
  },

  getCourseProgress: async (courseId: string) => {
    const response = await axiosInstance.get<Progress[]>(`/api/courses/${courseId}/progress`);
    return response.data;
  },

  getStudentCourseProgress: async (courseId: string, studentId: string) => {
    const response = await axiosInstance.get<Progress>(`/api/courses/${courseId}/students/${studentId}/progress`);
    return response.data;
  },
};
