import axiosInstance from './axios';
import { Lesson } from '../types';

export const lessonApi = {
  getCourseLessons: async (courseId: string) => {
    const response = await axiosInstance.get<Lesson[]>(`/api/courses/${courseId}/lessons`);
    return response.data;
  },

  getLessonById: async (lessonId: string) => {
    const response = await axiosInstance.get<Lesson>(`/api/lessons/${lessonId}`);
    return response.data;
  },

  createLesson: async (courseId: string, titleKk: string, contentKk: string, lessonOrder: number) => {
    const response = await axiosInstance.post<Lesson>(`/api/courses/${courseId}/lessons`, {
      courseId,
      titleKk,
      contentKk,
      lessonOrder,
    });
    return response.data;
  },

  updateLesson: async (lessonId: string, titleKk: string, contentKk: string) => {
    const response = await axiosInstance.put<Lesson>(`/api/lessons/${lessonId}`, {
      titleKk,
      contentKk,
    });
    return response.data;
  },

  deleteLesson: async (lessonId: string) => {
    await axiosInstance.delete(`/api/lessons/${lessonId}`);
  },

  publishLesson: async (lessonId: string) => {
    const response = await axiosInstance.patch<Lesson>(`/api/lessons/${lessonId}/publish`);
    return response.data;
  },

  unpublishLesson: async (lessonId: string) => {
    const response = await axiosInstance.patch<Lesson>(`/api/lessons/${lessonId}/unpublish`);
    return response.data;
  },
};
