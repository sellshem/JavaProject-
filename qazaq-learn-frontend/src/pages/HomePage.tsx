import { useNavigate } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { Button } from '../components/Button';
import { BookOpen, Users, Award } from 'lucide-react';

export const HomePage = () => {
  const navigate = useNavigate();

  return (
    <Layout>
      <div className="space-y-16">
        {/* Hero Section */}
        <section className="text-center py-20">
          <div className="inline-block mb-6">
            <BookOpen size={64} className="text-blue-600 mx-auto" />
          </div>
          <h1 className="text-5xl font-bold text-gray-900 mb-4">Қазақ Learn</h1>
          <p className="text-xl text-gray-600 mb-4">Қазақ тіліндегі өндіктік үйрету жүйесі</p>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto mb-8">
            Студенттер мен ұстаздар үшін замана ақпаратты технология арқылы өндіктік үйрету.
            Қашықтықтан немесе офистік формасында оқыңыз.
          </p>

          <div className="flex gap-4 justify-center flex-wrap">
            <Button onClick={() => navigate('/courses')} variant="primary" className="px-8 py-3">
              Курстарды қарау
            </Button>
            <Button onClick={() => navigate('/login')} variant="secondary" className="px-8 py-3">
              Кіру
            </Button>
            <Button onClick={() => navigate('/register')} variant="primary" className="px-8 py-3">
              Тіркеу
            </Button>
          </div>
        </section>

        {/* Features Section */}
        <section>
          <h2 className="text-3xl font-bold text-gray-900 mb-12 text-center">Мүмкіндіктер</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white rounded-lg shadow-lg p-8 text-center">
              <Users className="text-blue-600 mx-auto mb-4" size={48} />
              <h3 className="text-xl font-bold text-gray-900 mb-2">Студенттер үшін</h3>
              <p className="text-gray-600">
                Курстарға қатысыңыз, сабақтарды оқыңыз, тапсырмаларды орындаңыз және өзіңіздің ілгерілеуіңізді көріңіз.
              </p>
            </div>

            <div className="bg-white rounded-lg shadow-lg p-8 text-center">
              <Award className="text-green-600 mx-auto mb-4" size={48} />
              <h3 className="text-xl font-bold text-gray-900 mb-2">Ұстаздар үшін</h3>
              <p className="text-gray-600">
                Курстарды құрыңыз, сабақтарды басқарыңыз, тапсырмаларды орнатыңыз және студенттердің дәрістігін бағалаңыз.
              </p>
            </div>

            <div className="bg-white rounded-lg shadow-lg p-8 text-center">
              <BookOpen className="text-purple-600 mx-auto mb-4" size={48} />
              <h3 className="text-xl font-bold text-gray-900 mb-2">Админ үшін</h3>
              <p className="text-gray-600">
                Түпсін журналын көріңіз, қауіпсіздік сәтін бақылаңыз және жүйесін ілгерілеңіз.
              </p>
            </div>
          </div>
        </section>

        {/* Call to Action */}
        <section className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg shadow-lg p-12 text-center text-white">
          <h2 className="text-3xl font-bold mb-4">Бүгін бастаңыз</h2>
          <p className="text-lg mb-8">Оқу ісіне қатысыңыз немесе өз курсын құрыңыз</p>
          <Button onClick={() => navigate('/register')} className="bg-white text-blue-600 hover:bg-blue-50 px-8 py-3 font-semibold">
            Тіркеу
          </Button>
        </section>
      </div>
    </Layout>
  );
};
