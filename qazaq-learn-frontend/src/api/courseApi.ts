import axiosInstance from './axios';
import { Course } from '../types';

export const courseApi = {
  getAllCourses: async () => {
    const response = await axiosInstance.get<Course[]>('/api/courses');
    return response.data;
  },

  getCourseById: async (id: string) => {
    const response = await axiosInstance.get<Course>(`/api/courses/${id}`);
    return response.data;
  },

  createCourse: async (titleKk: string, descriptionKk: string) => {
    const response = await axiosInstance.post<Course>('/api/courses', {
      titleKk,
      descriptionKk,
    });
    return response.data;
  },

  updateCourse: async (id: string, titleKk: string, descriptionKk: string) => {
    const response = await axiosInstance.put<Course>(`/api/courses/${id}`, {
      titleKk,
      descriptionKk,
    });
    return response.data;
  },

  deleteCourse: async (id: string) => {
    await axiosInstance.delete(`/api/courses/${id}`);
  },

  publishCourse: async (id: string) => {
    const response = await axiosInstance.patch<Course>(`/api/courses/${id}/publish`);
    return response.data;
  },

  unpublishCourse: async (id: string) => {
    const response = await axiosInstance.patch<Course>(`/api/courses/${id}/unpublish`);
    return response.data;
  },
};
