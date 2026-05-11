# Qazaq Learn Frontend

Modern Learning Management System frontend for education in Kazakh language.

## Overview

Qazaq Learn is a professional web-based Learning Management System (LMS) built with React, designed specifically for educational institutions in Kazakhstan. It provides role-based access for students, teachers, and administrators with comprehensive course management, lesson planning, and progress tracking features.

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety and better DX
- **Vite** - Fast build tool and dev server
- **Tailwind CSS** - Utility-first styling
- **Axios** - HTTP client
- **React Router** - Client-side routing
- **ESLint** - Code quality
- **Lucide React** - Icon library

## Features

### Authentication
- User registration with role selection (Student/Teacher/Admin)
- JWT-based authentication
- Secure token storage in localStorage
- Automatic logout on 401 response
- Role-based navigation

### Student Dashboard
- View enrolled courses
- Complete lessons and track progress
- Submit assignments
- View grades and feedback
- Track personal progress percentage

### Teacher Dashboard
- Create and manage courses
- Add lessons and assignments
- Publish/unpublish content
- View enrolled students
- Grade student submissions
- Monitor course progress

### Admin Dashboard
- View system audit logs
- Monitor user activities
- Track all system actions

### Course Management
- Browse all courses
- View course details
- Enroll in courses (students)
- Manage course content (teachers)
- Track completion progress
- Submit and grade assignments

## Project Structure

```
src/
├── api/              # API endpoints
├── components/       # Reusable components
├── pages/            # Page components
├── types/            # TypeScript interfaces
├── utils/            # Utility functions
├── hooks/            # Custom React hooks
├── App.tsx           # Main app component
└── index.css         # Global styles
```

## Installation

### Prerequisites
- Node.js 16+ and npm

### Steps

1. Clone the repository:
```bash
cd qazaq-learn-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

4. Update `.env` with your backend URL:
```
VITE_API_BASE_URL=http://localhost:8080
```

## Running the Application

### Development Mode
```bash
npm run dev
```
The application will start at `http://localhost:3000`

### Production Build
```bash
npm run build
```

### Lint Code
```bash
npm run lint
```

### Preview Production Build
```bash
npm run preview
```

## Backend API

The frontend communicates with a Spring Boot backend at:
- **Development**: `http://localhost:8080`
- **API Base**: `/api`

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

#### Courses
- `GET /api/courses` - Get all courses
- `GET /api/courses/{id}` - Get course details
- `POST /api/courses` - Create course (teacher)
- `PUT /api/courses/{id}` - Update course (teacher)
- `DELETE /api/courses/{id}` - Delete course (teacher)
- `PATCH /api/courses/{id}/publish` - Publish course (teacher)

#### Lessons
- `GET /api/courses/{courseId}/lessons` - Get lessons
- `POST /api/courses/{courseId}/lessons` - Create lesson (teacher)
- `GET /api/lessons/{id}` - Get lesson details
- `PUT /api/lessons/{id}` - Update lesson (teacher)
- `PATCH /api/lessons/{id}/publish` - Publish lesson (teacher)

#### Enrollments
- `POST /api/courses/{courseId}/enroll` - Enroll in course (student)
- `DELETE /api/courses/{courseId}/enroll` - Unenroll from course (student)
- `GET /api/me/courses` - Get my courses (student)

#### Assignments
- `GET /api/courses/{courseId}/assignments` - Get assignments
- `POST /api/courses/{courseId}/assignments` - Create assignment (teacher)
- `GET /api/assignments/{id}` - Get assignment details
- `PUT /api/assignments/{id}` - Update assignment (teacher)

#### Submissions
- `POST /api/assignments/{assignmentId}/submissions` - Submit assignment (student)
- `GET /api/me/submissions` - Get my submissions (student)
- `GET /api/assignments/{assignmentId}/submissions` - Get submissions (teacher)
- `PATCH /api/submissions/{id}/grade` - Grade submission (teacher)

#### Progress
- `POST /api/lessons/{lessonId}/complete` - Mark lesson complete (student)
- `GET /api/me/progress` - Get my progress (student)
- `GET /api/courses/{courseId}/progress` - Get course progress (teacher)

#### Admin
- `GET /api/admin/audit-logs` - Get audit logs (admin)

## Pages

### Public Pages
- `/` - Home page with hero section
- `/login` - User login
- `/register` - User registration
- `/courses` - Browse all courses

### Protected Pages
- `/courses/:id` - Course details and content
- `/student` - Student dashboard
- `/teacher` - Teacher dashboard
- `/admin` - Admin dashboard

## User Roles

### Student
- View and enroll in courses
- Complete lessons and track progress
- Submit assignments
- View grades and feedback

### Teacher
- Create and manage courses
- Add and manage lessons
- Create and grade assignments
- View student progress

### Admin
- View system audit logs
- Monitor all activities

## Demo Credentials

For testing purposes (if available on backend):
- **Student**: `student@qazaq-learn.kz` / `password123`
- **Teacher**: `teacher@qazaq-learn.kz` / `password123`
- **Admin**: `admin@qazaq-learn.kz` / `password123`

## Authentication Flow

1. User registers or logs in
2. Backend returns JWT token, userId, email, and role
3. Frontend stores in localStorage
4. Axios interceptor adds token to all requests
5. On 401 response, frontend clears auth and redirects to login
6. Role-based routing controls access

## Components

### Layout Components
- `Layout` - Main layout wrapper
- `Navbar` - Navigation bar with role-based links

### Route Components
- `ProtectedRoute` - Guards authenticated routes
- `RoleRoute` - Guards role-specific routes

### UI Components
- `Button` - Reusable button with variants
- `Input` - Text input with validation
- `Select` - Dropdown select
- `Modal` - Modal dialog
- `LoadingSpinner` - Loading indicator
- `ErrorMessage` - Error display

### Business Components
- `CourseCard` - Course display card
- `LessonCard` - Lesson display card
- `AssignmentCard` - Assignment display card

## Error Handling

- **401 Unauthorized**: Auto-logout and redirect to login
- **403 Forbidden**: "Құқығыңыз жоқ" (You don't have permission)
- **500 Server Error**: "Сервер қатесі" (Server error)
- All errors display user-friendly messages in Kazakh

## Styling

All styling uses Tailwind CSS utility classes. The design features:
- Modern clean interface
- Responsive layout (mobile-first)
- Card-based design
- Professional shadows and spacing
- Rounded corners and borders
- Role-based color schemes
- University-style professional appearance

## Environment Variables

```
VITE_API_BASE_URL=http://localhost:8080
```

Available in code as: `import.meta.env.VITE_API_BASE_URL`

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance

- Fast development with Vite
- Optimized production build
- Code splitting with React Router
- Lazy loading components
- CSS optimization with Tailwind purging

## Future Enhancements

- Real-time notifications
- Video lessons support
- Discussion forums
- File uploads for assignments
- Grade book reports
- Attendance tracking
- Certificate generation
- Peer review system

## Contributing

1. Create a feature branch
2. Make your changes
3. Run linter: `npm run lint`
4. Build: `npm run build`
5. Submit a pull request

## Support

For issues or questions, please contact the development team or check the backend documentation.

## License

Copyright © 2024 Qazaq Learn. All rights reserved.
