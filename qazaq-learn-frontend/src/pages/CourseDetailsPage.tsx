import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { LessonCard } from '../components/LessonCard';
import { AssignmentCard } from '../components/AssignmentCard';
import { Button } from '../components/Button';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Modal } from '../components/Modal';
import { Input } from '../components/Input';
import { courseApi } from '../api/courseApi';
import { lessonApi } from '../api/lessonApi';
import { assignmentApi } from '../api/assignmentApi';
import { enrollmentApi } from '../api/enrollmentApi';
import { progressApi } from '../api/progressApi';
import { submissionApi } from '../api/submissionApi';
import { Course, Lesson, Assignment, Submission } from '../types';
import { authUtils, errorUtils } from '../utils/auth';

export const CourseDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [enrolled, setEnrolled] = useState(false);
  const [showLessonModal, setShowLessonModal] = useState(false);
  const [showAssignmentModal, setShowAssignmentModal] = useState(false);
  const [showSubmissionModal, setShowSubmissionModal] = useState(false);
  const [showSubmissionsModal, setShowSubmissionsModal] = useState(false);
  const [selectedAssignmentId, setSelectedAssignmentId] = useState('');
  const [selectedAssignmentForSubmissions, setSelectedAssignmentForSubmissions] = useState<Assignment | null>(null);
  const [assignmentSubmissions, setAssignmentSubmissions] = useState<Submission[]>([]);
  const [submissionsLoading, setSubmissionsLoading] = useState(false);
  const [gradingSubmissionId, setGradingSubmissionId] = useState<string | null>(null);
  const [gradeValue, setGradeValue] = useState('');
  const [feedbackValue, setFeedbackValue] = useState('');
  const [lessonTitle, setLessonTitle] = useState('');
  const [lessonContent, setLessonContent] = useState('');
  const [assignmentTitle, setAssignmentTitle] = useState('');
  const [assignmentDescription, setAssignmentDescription] = useState('');
  const [assignmentDeadline, setAssignmentDeadline] = useState('');
  const [submissionContent, setSubmissionContent] = useState('');
  const role = authUtils.getRole();
  const canInteract = role !== 'STUDENT' || enrolled;

  const isExpectedEnrollmentAccessError = (error: any, urlPart: string) => {
    const status = error?.response?.status;
    const requestUrl = error?.response?.config?.url ?? error?.config?.url;
    return (
      status === 403 &&
      role === 'STUDENT' &&
      !enrolled &&
      typeof requestUrl === 'string' &&
      requestUrl.includes(urlPart)
    );
  };

  const loadCourseDetails = async () => {
    if (!id) return;
    try {
      setError('');
      setLoading(true);

      const courseData = await courseApi.getCourseById(id);
      setCourse(courseData);

      let isEnrolled = enrolled;
      if (role === 'STUDENT') {
        const myEnrollments = await enrollmentApi.getMyEnrollments();
        isEnrolled = myEnrollments.some((c) => c.id === id);
        setEnrolled(isEnrolled);

        if (isEnrolled) {
          const mySubmissions = await submissionApi.getMySubmissions();
          setSubmissions(mySubmissions);
        } else {
          setSubmissions([]);
        }
      }

      try {
        const lessonsData = await lessonApi.getCourseLessons(id);
        setLessons(lessonsData);
      } catch (err) {
        if (isExpectedEnrollmentAccessError(err, `/api/courses/${id}/lessons`)) {
          setLessons([]);
        } else {
          throw err;
        }
      }

      try {
        const assignmentsData = await assignmentApi.getCourseAssignments(id);
        setAssignments(assignmentsData);
      } catch (err) {
        if (isExpectedEnrollmentAccessError(err, `/api/courses/${id}/assignments`)) {
          setAssignments([]);
        } else {
          throw err;
        }
      }
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCourseDetails();
  }, [id, role]);

  const handleEnroll = async () => {
    if (!id) return;
    try {
      await enrollmentApi.enrollInCourse(id);
      await loadCourseDetails();
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleCompleteLesson = async (lessonId: string) => {
    try {
      await progressApi.completeLesson(lessonId);
      alert('Сабақ аяқталды!');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleAddLesson = async () => {
    if (!id) return;
    try {
      const lessonOrder = lessons.length + 1;
      await lessonApi.createLesson(id, lessonTitle, lessonContent, lessonOrder);
      const lessonsData = await lessonApi.getCourseLessons(id);
      setLessons(lessonsData);
      setShowLessonModal(false);
      setLessonTitle('');
      setLessonContent('');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleAddAssignment = async () => {
    if (!id) return;
    try {
      const deadline = assignmentDeadline ? `${assignmentDeadline}T00:00:00` : null;
      await assignmentApi.createAssignment(id, assignmentTitle, assignmentDescription, deadline);
      const assignmentsData = await assignmentApi.getCourseAssignments(id);
      setAssignments(assignmentsData);
      setShowAssignmentModal(false);
      setAssignmentTitle('');
      setAssignmentDescription('');
      setAssignmentDeadline('');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleSubmitAssignment = async () => {
    if (!selectedAssignmentId || submissionContent.trim() === '') return;
    try {
      await submissionApi.submitAssignment(selectedAssignmentId, submissionContent.trim());
      const mySubmissions = await submissionApi.getMySubmissions();
      setSubmissions(mySubmissions);
      setShowSubmissionModal(false);
      setSubmissionContent('');
      alert('Тапсырма жөнелтілді!');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  const handleViewSubmissions = async (assignment: Assignment) => {
    setSelectedAssignmentForSubmissions(assignment);
    setAssignmentSubmissions([]);
    setShowSubmissionsModal(true);
    setSubmissionsLoading(true);
    try {
      const subs = await submissionApi.getAssignmentSubmissions(assignment.id);
      setAssignmentSubmissions(subs);
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    } finally {
      setSubmissionsLoading(false);
    }
  };

  const handleGradeSubmission = async (submissionId: string) => {
    if (!gradeValue || isNaN(Number(gradeValue))) return;
    setGradingSubmissionId(submissionId);
    try {
      const updated = await submissionApi.gradeSubmission(submissionId, Number(gradeValue), feedbackValue);
      setAssignmentSubmissions(prev => prev.map(s => s.id === submissionId ? updated : s));
      setGradeValue('');
      setFeedbackValue('');
      alert('Баға қойылды!');
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    } finally {
      setGradingSubmissionId(null);
    }
  };

  const handlePublishCourse = async () => {
    if (!id) return;
    try {
      await courseApi.publishCourse(id);
      const courseData = await courseApi.getCourseById(id);
      setCourse(courseData);
    } catch (err) {
      setError(errorUtils.getErrorMessage(err));
    }
  };

  if (loading) return <Layout><LoadingSpinner /></Layout>;
  if (!course) return <Layout><ErrorMessage message="Курс табылмады" /></Layout>;

  return (
    <Layout>
      <div className="space-y-8">
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">{course.titleKk}</h1>
          <p className="text-gray-600 text-lg mb-4">{course.descriptionKk}</p>
          <p className="text-gray-600 mb-6">Ұстаз: <span className="font-semibold">{course.teacherName}</span></p>

          {error && <ErrorMessage message={error} />}

          <div className="flex gap-4 flex-wrap">
            {!enrolled && role === 'STUDENT' && (
              <Button onClick={handleEnroll}>Қатысу</Button>
            )}
            {enrolled && role === 'STUDENT' && (
              <span className="text-green-600 font-semibold text-lg">✓ Сіз қатысқансыз</span>
            )}
            {role === 'TEACHER' && course.id && (
              <>
                <Button onClick={() => setShowLessonModal(true)}>Сабақ қосу</Button>
                <Button onClick={() => setShowAssignmentModal(true)}>Тапсырма қосу</Button>
                {!course.published && (
                  <Button onClick={handlePublishCourse}>Жарияланғыны</Button>
                )}
              </>
            )}
          </div>
        </div>

        {/* Lessons Section */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Сабақтар</h2>
          <div className="space-y-4">
            {lessons.map((lesson) => (
              <LessonCard
                key={lesson.id}
                lesson={lesson}
                onComplete={canInteract ? () => handleCompleteLesson(lesson.id) : undefined}
              />
            ))}
          </div>
          {lessons.length === 0 && <p className="text-gray-600">Сабақтар жоқ</p>}
        </div>

        {/* Assignments Section */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Тапсырмалар</h2>
          <div className="space-y-4">
            {assignments.map((assignment) => (
              <AssignmentCard
                key={assignment.id}
                assignment={assignment}
                onSubmit={canInteract ? () => {
                  setSelectedAssignmentId(assignment.id);
                  setShowSubmissionModal(true);
                } : undefined}
                onViewSubmissions={role === 'TEACHER' ? () => handleViewSubmissions(assignment) : undefined}
                isSubmitted={submissions.some((s) => s.assignmentId === assignment.id)}
              />
            ))}
          </div>
          {assignments.length === 0 && <p className="text-gray-600">Тапсырмалар жоқ</p>}
        </div>
      </div>

      {/* Add Lesson Modal */}
      <Modal
        title="Сабақ қосу"
        isOpen={showLessonModal}
        onClose={() => setShowLessonModal(false)}
      >
        <div className="space-y-4">
          <Input
            label="Сабақтың құрылымы"
            value={lessonTitle}
            onChange={(e) => setLessonTitle(e.target.value)}
            placeholder="Сабақтың құрылымы"
          />
          <textarea
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
            placeholder="Сабақтың мазмұны"
            value={lessonContent}
            onChange={(e) => setLessonContent(e.target.value)}
          />
          <Button onClick={handleAddLesson} className="w-full">
            Қосу
          </Button>
        </div>
      </Modal>

      {/* Add Assignment Modal */}
      <Modal
        title="Тапсырма қосу"
        isOpen={showAssignmentModal}
        onClose={() => setShowAssignmentModal(false)}
      >
        <div className="space-y-4">
          <Input
            label="Тапсырманың құрылымы"
            value={assignmentTitle}
            onChange={(e) => setAssignmentTitle(e.target.value)}
            placeholder="Тапсырманың құрылымы"
          />
          <textarea
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
            placeholder="Тапсырманың мазмұны"
            value={assignmentDescription}
            onChange={(e) => setAssignmentDescription(e.target.value)}
          />
          <Input
            label="Мерзімі"
            type="date"
            value={assignmentDeadline}
            onChange={(e) => setAssignmentDeadline(e.target.value)}
          />
          <Button onClick={handleAddAssignment} className="w-full">
            Қосу
          </Button>
        </div>
      </Modal>

      {/* Submit Assignment Modal */}
      <Modal
        title="Тапсырма жөнелту"
        isOpen={showSubmissionModal}
        onClose={() => setShowSubmissionModal(false)}
      >
        <div className="space-y-4">
          <textarea
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={6}
            placeholder="Өзіңіздің жауабыңызды жазыңыз"
            value={submissionContent}
            onChange={(e) => setSubmissionContent(e.target.value)}
          />
          <Button
            onClick={handleSubmitAssignment}
            className="w-full"
            disabled={submissionContent.trim() === ''}
          >
            Жөнелту
          </Button>
        </div>
      </Modal>

      {/* View Submissions Modal */}
      <Modal
        title={`${selectedAssignmentForSubmissions?.titleKk} - Жауаптар`}
        isOpen={showSubmissionsModal}
        onClose={() => setShowSubmissionsModal(false)}
      >
        {submissionsLoading ? (
          <LoadingSpinner />
        ) : assignmentSubmissions.length === 0 ? (
          <p className="text-gray-600">Жауаптар жоқ</p>
        ) : (
          <div className="space-y-4">
            {assignmentSubmissions.map((submission) => (
              <div key={submission.id} className="border border-gray-200 rounded-lg p-4">
                <div className="mb-2">
                  <p className="font-semibold">{submission.studentFullName} ({submission.studentEmail})</p>
                  <p className="text-sm text-gray-600">Жөнелтілді: {new Date(submission.submittedAt).toLocaleString('kk-KZ')}</p>
                  <p className="text-sm text-gray-600">Статус: {submission.status}</p>
                  {submission.grade !== undefined && (
                    <p className="text-sm text-green-600">Баға: {submission.grade}/100</p>
                  )}
                  {submission.gradedAt && (
                    <p className="text-sm text-gray-600">Бағаланды: {new Date(submission.gradedAt).toLocaleString('kk-KZ')}</p>
                  )}
                </div>
                <div className="mb-4">
                  <p className="text-gray-800">{submission.answerText}</p>
                </div>
                {submission.feedbackKk && (
                  <div className="mb-4 p-2 bg-blue-50 rounded">
                    <p className="text-sm font-semibold text-blue-800">Пікір:</p>
                    <p className="text-sm text-blue-700">{submission.feedbackKk}</p>
                  </div>
                )}
                {submission.status !== 'GRADED' && (
                  <div className="space-y-2">
                    <Input
                      label="Баға (0-100)"
                      type="number"
                      min="0"
                      max="100"
                      value={gradeValue}
                      onChange={(e) => setGradeValue(e.target.value)}
                      placeholder="Баға"
                    />
                    <textarea
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={3}
                      placeholder="Пікір"
                      value={feedbackValue}
                      onChange={(e) => setFeedbackValue(e.target.value)}
                    />
                    <Button
                      onClick={() => handleGradeSubmission(submission.id)}
                      disabled={gradingSubmissionId === submission.id || !gradeValue}
                      className="w-full"
                    >
                      {gradingSubmissionId === submission.id ? 'Бағалау...' : 'Бағалау'}
                    </Button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Modal>
    </Layout>
  );
};
