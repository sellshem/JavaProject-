import { Lesson } from '../types';
import { FileText, Check } from 'lucide-react';

interface LessonCardProps {
  lesson: Lesson;
  isCompleted?: boolean;
  onComplete?: () => void;
  onSelect?: () => void;
}

export const LessonCard = ({ lesson, isCompleted, onComplete, onSelect }: LessonCardProps) => {
  return (
    <div className="bg-white rounded-lg shadow p-4 hover:shadow-lg transition">
      <div className="flex items-start space-x-3">
        <div className="bg-green-100 p-2 rounded-lg">
          <FileText className="text-green-600" size={20} />
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900">{lesson.titleKk}</h3>
          <p className="text-gray-600 text-sm mt-1 line-clamp-2">{lesson.contentKk}</p>
          <div className="flex items-center justify-between mt-4">
            <span className={`text-xs font-semibold px-2 py-1 rounded ${
              lesson.published ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
            }`}>
              {lesson.published ? 'Жарияланған' : 'Черновик'}
            </span>
            <div className="flex space-x-2">
              {onSelect && (
                <button onClick={onSelect} className="text-blue-600 hover:text-blue-700 text-sm font-semibold">
                  Оқу
                </button>
              )}
              {onComplete && !isCompleted && (
                <button
                  onClick={onComplete}
                  className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm transition"
                >
                  Аяқтау
                </button>
              )}
              {isCompleted && (
                <div className="flex items-center space-x-1 text-green-600 text-sm font-semibold">
                  <Check size={16} />
                  <span>Аяқталды</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
