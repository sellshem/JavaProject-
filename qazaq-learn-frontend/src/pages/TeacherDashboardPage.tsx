import { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { Button } from '../components/Button';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Modal } from '../components/Modal';
import { Input } from '../components/Input';
import { Course, CourseStudent } from '../types';
import { courseApi } from '../api/courseApi';
import { enrollmentApi } from '../api/enrollmentApi';
import { errorUtils } from '../utils/auth';

export const TeacherDashboardPage = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [courseTitle, setCourseTitle] = useState('');
  const [courseDescription, setCourseDescription] = useState('');
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [showStudentsModal, setShowStudentsModal] = useState(false);
  const [students, setStudents] = useState<CourseStudent[]>([]);
  const [studentsLoading, setStudentsLoading] = useState(false);
  const [studentsError, setStudentsError] = useState('');

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const coursesData = await courseApi.getAllCourses();
        // Filter to show only this teacher's courses
        setCourses(coursesData);
      } catch (err) {
        setError(errorUtils.getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchCourses();
  }, []);

  const handleCreateCourse = async () => {
    try {
      await courseApi.createCourse(courseTitle, courseDescription);
      const coursesData = await courseApi.getAllCourses();
      setCourses(coursesData);
      setShowCreateModal(false);
      setCourseTitle('');
      setCourseDescription('');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleDeleteCourse = async (id: string) => {
    if (!window.confirm('Бұл курсты өшіргіңіз келеме?')) return;
    try {
      await courseApi.deleteCourse(id);
      const coursesData = await courseApi.getAllCourses();
      setCourses(coursesData);
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleViewStudents = async (course: Course) => {
    setSelectedCourse(course);
    setStudents([]);
    setStudentsError('');
    setShowStudentsModal(true);
    setStudentsLoading(true);
    try {
      const studentsData = await enrollmentApi.getCourseStudents(course.id);
      setStudents(studentsData);
    } catch (err) {
      setStudentsError(errorUtils.getErrorMessage(err));
    } finally {
      setStudentsLoading(false);
    }
  };

  if (loading) return <Layout><LoadingSpinner /></Layout>;

  return (
    <Layout>
      <div className="space-y-8">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-4xl font-bold text-gray-900 mb-2">Ұстаздың панелі</h1>
            <p className="text-gray-600">Өз курстарыңызды басқарыңыз</p>
          </div>
          <Button onClick={() => setShowCreateModal(true)}>+ Жаңа курс құру</Button>
        </div>

        {error && <ErrorMessage message={error} />}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {courses.map((course) => (
            <div key={course.id} className="bg-white rounded-lg shadow-lg p-6">
              <h3 className="text-xl font-bold text-gray-900 mb-2">{course.titleKk}</h3>
              <p className="text-gray-600 mb-4 line-clamp-2">{course.descriptionKk}</p>
              
              <div className="mb-4">
                <span
                  className={`text-xs font-semibold px-3 py-1 rounded ${
                    course.published ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                  }`}
                >
                  {course.published ? 'Жарияланған' : 'Черновик'}
                </span>
              </div>

              <div className="flex gap-2 flex-wrap">
                <Button
                  onClick={() => window.location.href = `/courses/${course.id}`}
                  variant="secondary"
                >
                  Көру
                </Button>
                <Button onClick={() => handleViewStudents(course)}>
                  Студенттер
                </Button>
                <Button
                  onClick={() => handleDeleteCourse(course.id)}
                  variant="danger"
                >
                  Өшіру
                </Button>
              </div>
            </div>
          ))}
        </div>

        {courses.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-600 text-lg mb-4">Құрста жоқ</p>
            <Button onClick={() => setShowCreateModal(true)}>Бірінші курсты құру</Button>
          </div>
        )}
      </div>

      {/* Create Course Modal */}
      <Modal
        title="Жаңа курс құру"
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
      >
        <div className="space-y-4">
          <Input
            label="Курстың құрылымы (Қазақша)"
            value={courseTitle}
            onChange={(e) => setCourseTitle(e.target.value)}
            placeholder="Курстың құрылымы"
          />
          <textarea
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
            placeholder="Курстың сипаттамасы (Қазақша)"
            value={courseDescription}
            onChange={(e) => setCourseDescription(e.target.value)}
          />
          <Button onClick={handleCreateCourse} className="w-full">
            Құру
          </Button>
        </div>
      </Modal>

      {/* Students Modal */}
      <Modal
        title={selectedCourse ? `${selectedCourse.titleKk} - Студенттер` : 'Студенттер'}
        isOpen={showStudentsModal}
        onClose={() => setShowStudentsModal(false)}
      >
        <div className="space-y-4">
          {studentsLoading && (
            <div className="flex justify-center py-8">
              <LoadingSpinner />
            </div>
          )}

          {studentsError && !studentsLoading && (
            <ErrorMessage message={studentsError} />
          )}

          {!studentsLoading && !studentsError && students.length === 0 && (
            <div className="text-center py-8">
              <p className="text-gray-600 text-lg mb-2">Бұл курсқа студенттер тіркелмеген</p>
              <p className="text-gray-500">Студенттер курсты тіркелген кезде олар осы жерде көрсетіледі.</p>
            </div>
          )}

          {!studentsLoading && !studentsError && students.length > 0 && (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Студент</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Тіркелген күні</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Прогресс</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Сабақтар</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Тапсырмалар</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {students.map((student) => (
                    <tr key={student.studentId}>
                      <td className="px-4 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{student.fullName}</td>
                      <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-600">{student.email}</td>
                      <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-600">{new Date(student.enrolledAt).toLocaleDateString('kk-KZ')}</td>
                      <td className="px-4 py-4 whitespace-nowrap">
                        <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                          <div
                            className="h-3 bg-green-600"
                            style={{ width: `${student.progressPercent}%` }}
                          />
                        </div>
                        <div className="mt-1 text-xs text-gray-600">{student.progressPercent}%</div>
                      </td>
                      <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-600">
                        {student.completedLessonsCount}/{student.totalLessonsCount}
                      </td>
                      <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-600">
                        {student.submittedAssignmentsCount}/{student.totalAssignmentsCount}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </Modal>
    </Layout>
  );
};
