export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  role: UserRole;
}

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface Course {
  id: string;
  titleKk: string;
  descriptionKk: string;
  teacherId: string;
  teacherName: string;
  published: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Lesson {
  id: string;
  courseId: string;
  titleKk: string;
  contentKk: string;
  published: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Enrollment {
  id: string;
  courseId: string;
  studentId: string;
  studentEmail: string;
  enrolledAt: string;
}

export interface CourseStudent {
  studentId: string;
  fullName: string;
  email: string;
  enrolledAt: string;
  completedLessonsCount: number;
  totalLessonsCount: number;
  submittedAssignmentsCount: number;
  totalAssignmentsCount: number;
  progressPercent: number;
}

export interface Progress {
  id: string;
  studentId: string;
  courseId: string;
  lessonId?: string;
  completedLessons: number;
  totalLessons: number;
  completionPercentage: number;
  lastProgressAt: string;
}

export interface Assignment {
  id: string;
  courseId: string;
  lessonId?: string;
  titleKk: string;
  descriptionKk: string;
  deadline: string;
  createdAt: string;
  updatedAt: string;
}

export interface Submission {
  id: string;
  assignmentId: string;
  assignmentTitle: string;
  studentId: string;
  studentFullName: string;
  studentEmail: string;
  answerText: string;
  grade?: number;
  feedbackKk?: string;
  submittedAt: string;
  gradedAt?: string;
  status: string;
}

export interface AuditLog {
  id: string;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  timestamp: string;
  ipAddress: string;
}

export interface ApiError {
  message: string;
  status?: number;
}
