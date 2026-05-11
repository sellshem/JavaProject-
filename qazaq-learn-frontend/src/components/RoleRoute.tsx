import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { authUtils } from '../utils/auth';
import { UserRole } from '../types';

interface RoleRouteProps {
  children: ReactNode;
  allowedRoles: UserRole[];
}

export const RoleRoute = ({ children, allowedRoles }: RoleRouteProps) => {
  const role = authUtils.getRole();

  if (!authUtils.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  if (!role || !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};
