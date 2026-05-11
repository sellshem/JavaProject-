import axiosInstance from './axios';
import { Submission } from '../types';

export const submissionApi = {
  submitAssignment: async (assignmentId: string, answerText: string) => {
    const response = await axiosInstance.post<Submission>(`/api/assignments/${assignmentId}/submissions`, {
      answerText,
    });
    return response.data;
  },

  getMySubmissions: async () => {
    const response = await axiosInstance.get<Submission[]>('/api/me/submissions');
    return response.data;
  },

  getAssignmentSubmissions: async (assignmentId: string) => {
    const response = await axiosInstance.get<Submission[]>(`/api/assignments/${assignmentId}/submissions`);
    return response.data;
  },

  gradeSubmission: async (submissionId: string, grade: number, feedbackKk: string) => {
    const response = await axiosInstance.patch<Submission>(`/api/submissions/${submissionId}/grade`, {
      grade,
      feedbackKk,
    });
    return response.data;
  },
};
