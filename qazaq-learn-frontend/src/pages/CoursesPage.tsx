import { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { CourseCard } from '../components/CourseCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { courseApi } from '../api/courseApi';
import { enrollmentApi } from '../api/enrollmentApi';
import { Course } from '../types';
import { authUtils, errorUtils } from '../utils/auth';

export const CoursesPage = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [enrolledCourses, setEnrolledCourses] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const role = authUtils.getRole();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const coursesData = await courseApi.getAllCourses();
        setCourses(coursesData);

        if (role === 'STUDENT') {
          const myEnrollments = await enrollmentApi.getMyEnrollments();
          setEnrolledCourses(myEnrollments.map((c) => c.id));
        }
      } catch (err) {
        setError(errorUtils.getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [role]);

  const handleEnroll = async (courseId: string) => {
    try {
      await enrollmentApi.enrollInCourse(courseId);
      setEnrolledCourses([...enrolledCourses, courseId]);
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  if (loading) return <Layout><LoadingSpinner /></Layout>;

  return (
    <Layout>
      <div className="space-y-8">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Курстар</h1>
          <p className="text-gray-600">Барлық қол жеткіліктіліді курстар</p>
        </div>

        {error && <ErrorMessage message={error} />}

        <div className="space-y-4">
          {courses.map((course) => (
            <CourseCard
              key={course.id}
              course={course}
              isEnrolled={enrolledCourses.includes(course.id)}
              onEnroll={role === 'STUDENT' ? () => handleEnroll(course.id) : undefined}
            />
          ))}
        </div>

        {courses.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-600 text-lg">Курстар табылмады</p>
          </div>
        )}
      </div>
    </Layout>
  );
};
