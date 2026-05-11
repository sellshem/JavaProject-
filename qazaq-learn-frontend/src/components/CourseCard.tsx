import { Course } from '../types';
import { Link } from 'react-router-dom';
import { BookOpen, ArrowRight } from 'lucide-react';

interface CourseCardProps {
  course: Course;
  onEnroll?: () => void;
  onManage?: () => void;
  isEnrolled?: boolean;
}

export const CourseCard = ({ course, onEnroll, onManage, isEnrolled }: CourseCardProps) => {
  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition p-6">
      <div className="flex items-start space-x-4">
        <div className="bg-blue-100 p-3 rounded-lg">
          <BookOpen className="text-blue-600" size={24} />
        </div>
        <div className="flex-1">
          <h3 className="text-lg font-bold text-gray-900 mb-2">{course.titleKk}</h3>
          <p className="text-gray-600 text-sm mb-3">{course.descriptionKk}</p>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-500">Ұстаз: {course.teacherName}</p>
              <span
                className={`text-xs font-semibold mt-1 inline-block px-3 py-1 rounded ${
                  course.published ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                }`}
              >
                {course.published ? 'Жарияланған' : 'Черновик'}
              </span>
            </div>
            <div className="flex space-x-2">
              <Link
                to={`/courses/${course.id}`}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg text-sm flex items-center space-x-1 transition"
              >
                <span>Қара</span>
                <ArrowRight size={16} />
              </Link>
              {onEnroll && !isEnrolled && (
                <button
                  onClick={onEnroll}
                  className="bg-green-600 hover:bg-green-700 text-white px-3 py-2 rounded-lg text-sm transition"
                >
                  Қатысу
                </button>
              )}
              {onManage && (
                <button
                  onClick={onManage}
                  className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-2 rounded-lg text-sm transition"
                >
                  Өңдеу
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
