import axiosInstance from './axios';
import { AuditLog } from '../types';

export const auditApi = {
  getAuditLogs: async () => {
    const response = await axiosInstance.get<AuditLog[]>('/api/admin/audit-logs');
    return response.data;
  },
};
