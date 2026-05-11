import { AlertCircle } from 'lucide-react';

interface ErrorMessageProps {
  message: string;
}

export const ErrorMessage = ({ message }: ErrorMessageProps) => {
  return (
    <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-4 rounded-lg flex items-center space-x-3">
      <AlertCircle size={20} />
      <span>{message}</span>
    </div>
  );
};
