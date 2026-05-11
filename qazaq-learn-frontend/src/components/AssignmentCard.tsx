import { Assignment } from '../types';
import { Calendar, CheckCircle } from 'lucide-react';

interface AssignmentCardProps {
  assignment: Assignment;
  onSubmit?: () => void;
  onManage?: () => void;
  onViewSubmissions?: () => void;
  isSubmitted?: boolean;
}

export const AssignmentCard = ({ assignment, onSubmit, onManage, onViewSubmissions, isSubmitted }: AssignmentCardProps) => {
  return (
    <div className="bg-white rounded-lg shadow p-4 hover:shadow-lg transition">
      <div className="flex items-start space-x-3">
        <div className="bg-orange-100 p-2 rounded-lg">
          <CheckCircle className="text-orange-600" size={20} />
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900">{assignment.titleKk}</h3>
          <p className="text-gray-600 text-sm mt-1">{assignment.descriptionKk}</p>
          <div className="flex items-center space-x-2 mt-3 text-gray-500 text-sm">
            <Calendar size={16} />
            <span>Мерзімі: {assignment.deadline ? new Date(assignment.deadline).toLocaleDateString('kk-KZ') : 'Мерзімі көрсетілмеген'}</span>
          </div>
          <div className="flex space-x-2 mt-4">
            {onSubmit && !isSubmitted && (
              <button
                onClick={onSubmit}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-sm transition"
              >
                Жөнелту
              </button>
            )}
            {isSubmitted && (
              <span className="text-green-600 font-semibold text-sm flex items-center space-x-1">
                <CheckCircle size={16} />
                <span>Жөнелтілді</span>
              </span>
            )}
            {onManage && (
              <button
                onClick={onManage}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded text-sm transition"
              >
                Басқару
              </button>
            )}
            {onViewSubmissions && (
              <button
                onClick={onViewSubmissions}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded text-sm transition"
              >
                Жауаптарды тексеру
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
