import { Link, useNavigate } from 'react-router-dom';
import { LogOut, BookOpen, LogIn } from 'lucide-react';
import { authUtils } from '../utils/auth';

export const Navbar = () => {
  const navigate = useNavigate();
  const isAuthenticated = authUtils.isAuthenticated();
  const role = authUtils.getRole();
  const email = localStorage.getItem('email');

  const handleLogout = () => {
    authUtils.clearAuthData();
    navigate('/');
  };

  return (
    <nav className="bg-gradient-to-r from-blue-600 to-blue-800 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 font-bold text-xl hover:text-blue-100 transition">
            <BookOpen size={28} />
            <span>Qazaq Learn</span>
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center space-x-6">
            <Link to="/courses" className="hover:text-blue-100 transition flex items-center space-x-1">
              <span>Курстар</span>
            </Link>

            {isAuthenticated ? (
              <>
                {role === 'STUDENT' && (
                  <Link to="/student" className="hover:text-blue-100 transition flex items-center space-x-1">
                    <span>Менің курстарым</span>
                  </Link>
                )}
                {role === 'TEACHER' && (
                  <Link to="/teacher" className="hover:text-blue-100 transition flex items-center space-x-1">
                    <span>Ұстаздың панелі</span>
                  </Link>
                )}
                {role === 'ADMIN' && (
                  <Link to="/admin" className="hover:text-blue-100 transition flex items-center space-x-1">
                    <span>Әкімші панелі</span>
                  </Link>
                )}
              </>
            ) : null}

            {isAuthenticated ? (
              <>
                <div className="border-l border-blue-400 pl-6">
                  <div className="text-sm text-blue-100 mb-2">{email}</div>
                  <button
                    onClick={handleLogout}
                    className="bg-red-500 hover:bg-red-600 px-4 py-2 rounded-lg flex items-center space-x-2 transition"
                  >
                    <LogOut size={18} />
                    <span>Шығу</span>
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="bg-white text-blue-600 px-4 py-2 rounded-lg hover:bg-blue-50 transition flex items-center space-x-1 font-semibold"
                >
                  <LogIn size={18} />
                  <span>Кіру</span>
                </Link>
                <Link
                  to="/register"
                  className="bg-green-500 hover:bg-green-600 px-4 py-2 rounded-lg transition font-semibold"
                >
                  Тіркеу
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};
