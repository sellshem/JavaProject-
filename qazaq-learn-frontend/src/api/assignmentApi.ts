import axiosInstance from './axios';
import { Assignment } from '../types';

export const assignmentApi = {
  getCourseAssignments: async (courseId: string) => {
    const response = await axiosInstance.get<Assignment[]>(`/api/courses/${courseId}/assignments`);
    return response.data;
  },

  getAssignmentById: async (assignmentId: string) => {
    const response = await axiosInstance.get<Assignment>(`/api/assignments/${assignmentId}`);
    return response.data;
  },

  createAssignment: async (courseId: string, titleKk: string, descriptionKk: string, deadline: string | null) => {
    const response = await axiosInstance.post<Assignment>(`/api/courses/${courseId}/assignments`, {
      courseId,
      titleKk,
      descriptionKk,
      deadline,
    });
    return response.data;
  },

  updateAssignment: async (assignmentId: string, titleKk: string, descriptionKk: string, deadline: string | null) => {
    const response = await axiosInstance.put<Assignment>(`/api/assignments/${assignmentId}`, {
      titleKk,
      descriptionKk,
      deadline,
    });
    return response.data;
  },

  deleteAssignment: async (assignmentId: string) => {
    await axiosInstance.delete(`/api/assignments/${assignmentId}`);
  },
};
