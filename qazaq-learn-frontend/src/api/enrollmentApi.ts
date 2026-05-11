import axiosInstance from './axios';
import { Course, CourseStudent, Enrollment } from '../types';

export const enrollmentApi = {
  enrollInCourse: async (courseId: string) => {
    const response = await axiosInstance.post<Enrollment>(`/api/courses/${courseId}/enroll`);
    return response.data;
  },

  unenrollFromCourse: async (courseId: string) => {
    await axiosInstance.delete(`/api/courses/${courseId}/enroll`);
  },

  getMyEnrollments: async () => {
    const response = await axiosInstance.get<Course[]>('/api/me/courses');
    return response.data;
  },

  getCourseStudents: async (courseId: string) => {
    const response = await axiosInstance.get<CourseStudent[]>(`/api/courses/${courseId}/students`);
    return response.data;
  },
};
