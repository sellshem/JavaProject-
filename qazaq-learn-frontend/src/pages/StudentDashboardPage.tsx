import { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { CourseCard } from '../components/CourseCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Course, Progress, Submission } from '../types';
import { enrollmentApi } from '../api/enrollmentApi';
import { progressApi } from '../api/progressApi';
import { submissionApi } from '../api/submissionApi';
import { errorUtils } from '../utils/auth';

export const StudentDashboardPage = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [progress, setProgress] = useState<Progress[]>([]);
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const coursesData = await enrollmentApi.getMyEnrollments();
        setCourses(coursesData);

        const progressData = await progressApi.getMyProgress();
        setProgress(progressData);

        const submissionsData = await submissionApi.getMySubmissions();
        setSubmissions(submissionsData);
      } catch (err) {
        setError(errorUtils.getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) return <Layout><LoadingSpinner /></Layout>;

  return (
    <Layout>
      <div className="space-y-8">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Менің курстарым</h1>
          <p className="text-gray-600">Студенттің панелі</p>
        </div>

        {error && <ErrorMessage message={error} />}

        {/* Progress Overview */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Сіздің ілгерілеуіңіз</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {progress.map((p) => (
              <div key={p.id} className="bg-white rounded-lg shadow p-6">
                <p className="text-gray-600 mb-2">Курс ID: {p.courseId}</p>
                <div className="mb-4">
                  <div className="flex justify-between mb-2">
                    <span className="font-semibold">{p.completedLessons}/{p.totalLessons}</span>
                    <span className="text-gray-600">{p.completionPercentage}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-green-600 h-2 rounded-full"
                      style={{ width: `${p.completionPercentage}%` }}
                    ></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* My Courses */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Менің курстарым ({courses.length})</h2>
          <div className="space-y-4">
            {courses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
          {courses.length === 0 && <p className="text-gray-600">Курстарға қатысты емессіз</p>}
        </div>

        {/* Submissions */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Менің жөнелтулерім ({submissions.length})</h2>
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Тапсырма</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Жөнелтулі</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Баллы</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Пікір</th>
                </tr>
              </thead>
              <tbody>
                {submissions.map((submission) => (
                  <tr key={submission.id} className="border-t">
                    <td className="px-6 py-3 text-sm text-gray-700">{submission.assignmentTitle || "Белгісіз тапсырма"}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">
                      {new Date(submission.submittedAt).toLocaleDateString('kk-KZ')}
                    </td>
                    <td className="px-6 py-3 text-sm text-gray-700">{submission.grade || '-'}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{submission.feedbackKk || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {submissions.length === 0 && (
              <div className="px-6 py-8 text-center text-gray-600">
                Сіз әлі ешқандай тапсырма жөнелтпедіңіз
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};
