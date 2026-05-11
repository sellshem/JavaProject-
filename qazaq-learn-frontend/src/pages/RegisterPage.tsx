import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { Input } from '../components/Input';
import { Select } from '../components/Select';
import { Button } from '../components/Button';
import { ErrorMessage } from '../components/ErrorMessage';
import { authApi } from '../api/authApi';
import { authUtils, errorUtils } from '../utils/auth';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('STUDENT');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

   const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authApi.register(fullName, email, password, role);
      authUtils.saveAuthData(response.accessToken, response.refreshToken, response.userId, response.email, response.role);

      // Redirect based on role
      if (response.role === 'TEACHER') {
        navigate('/teacher');
      } else if (response.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/student');
      }
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="min-h-screen flex items-center justify-center -my-8">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Қазақ Learn</h1>
          <p className="text-gray-600 mb-6">Аккаунт құру</p>

          {error && <ErrorMessage message={error} />}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Толық аты"
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Иван Иванов"
              required
            />

            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
              required
            />

            <Input
              label="Пароль"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />

            <Select
              label="Рөлі"
              value={role}
              onChange={(e) => setRole(e.target.value)}
              options={[
                { value: 'STUDENT', label: 'Студент' },
                { value: 'TEACHER', label: 'Ұстаз' },
              ]}
            />

            <Button type="submit" loading={loading} className="w-full">
              Тіркеу
            </Button>
          </form>

          <p className="text-center text-gray-600 mt-4">
            Аккаунты бар ма?{' '}
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
              Кіру
            </Link>
          </p>
        </div>
      </div>
    </Layout>
  );
};
