# Qazaq Learn Frontend - Setup & Deployment Guide

## Project Created Successfully! ✓

Your modern React-based Learning Management System frontend has been created at:
```
c:\Users\mutal\Desktop\JAVA\qazaq-learn-frontend
```

## Quick Start

### 1. Install Dependencies

Open PowerShell and navigate to the project:
```powershell
cd c:\Users\mutal\Desktop\JAVA\qazaq-learn-frontend
npm install
```

This will install all required packages:
- React 18
- React Router
- Axios (HTTP client)
- TypeScript
- Tailwind CSS
- Vite (build tool)
- And more...

### 2. Create Environment File

The `.env.example` file already exists. Create `.env` from it:
```bash
copy .env.example .env
```

Update if needed (default should work):
```
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Start Development Server

```bash
npm run dev
```

The application will automatically open at: `http://localhost:3000`

## Project Structure

```
qazaq-learn-frontend/
├── src/
│   ├── api/                    # Backend API calls
│   │   ├── axios.ts           # Axios configuration with interceptors
│   │   ├── authApi.ts         # Auth endpoints
│   │   ├── courseApi.ts       # Course management
│   │   ├── lessonApi.ts       # Lesson management
│   │   ├── enrollmentApi.ts   # Enrollment endpoints
│   │   ├── progressApi.ts     # Progress tracking
│   │   ├── assignmentApi.ts   # Assignments
│   │   ├── submissionApi.ts   # Submissions
│   │   └── auditApi.ts        # Admin audit logs
│   ├── components/             # Reusable UI components
│   │   ├── Layout.tsx         # Main layout wrapper
│   │   ├── Navbar.tsx         # Navigation bar
│   │   ├── ProtectedRoute.tsx # Auth guard
│   │   ├── RoleRoute.tsx      # Role-based guard
│   │   ├── Button.tsx         # Button component
│   │   ├── Input.tsx          # Input component
│   │   ├── Select.tsx         # Dropdown component
│   │   ├── Modal.tsx          # Modal dialog
│   │   ├── LoadingSpinner.tsx # Loading indicator
│   │   ├── ErrorMessage.tsx   # Error display
│   │   ├── CourseCard.tsx     # Course card
│   │   ├── LessonCard.tsx     # Lesson card
│   │   └── AssignmentCard.tsx # Assignment card
│   ├── pages/                  # Page components
│   │   ├── HomePage.tsx       # Home/landing page
│   │   ├── LoginPage.tsx      # Login page
│   │   ├── RegisterPage.tsx   # Registration page
│   │   ├── CoursesPage.tsx    # Courses listing
│   │   ├── CourseDetailsPage.tsx   # Course details & content
│   │   ├── StudentDashboardPage.tsx # Student dashboard
│   │   ├── TeacherDashboardPage.tsx # Teacher dashboard
│   │   └── AdminDashboardPage.tsx   # Admin dashboard
│   ├── types/                  # TypeScript interfaces
│   │   └── index.ts           # All type definitions
│   ├── utils/                  # Utility functions
│   │   └── auth.ts            # Auth utilities
│   ├── App.tsx                # Main app with routing
│   ├── main.tsx               # Entry point
│   └── index.css              # Global styles (Tailwind)
├── public/                     # Static assets
├── package.json               # Dependencies
├── tsconfig.json              # TypeScript config
├── vite.config.ts             # Vite configuration
├── tailwind.config.js         # Tailwind configuration
├── postcss.config.js          # PostCSS configuration
├── .eslintrc.cjs              # ESLint rules
├── .gitignore                 # Git ignore file
├── .env.example               # Environment template
├── index.html                 # HTML entry point
└── README.md                  # Project documentation
```

## Available Pages

### Public Pages (No Login Required)
- `http://localhost:3000/` - Home page
- `http://localhost:3000/login` - Login page
- `http://localhost:3000/register` - Registration page
- `http://localhost:3000/courses` - Browse courses

### Protected Pages (Login Required)
- `http://localhost:3000/courses/:id` - Course details
- `http://localhost:3000/student` - Student dashboard (students only)
- `http://localhost:3000/teacher` - Teacher dashboard (teachers only)
- `http://localhost:3000/admin` - Admin dashboard (admins only)

## Available npm Commands

```bash
# Start development server (with hot reload)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run ESLint to check code quality
npm run lint
```

## Authentication Flow

1. **Registration**: User fills in name, email, password, and selects role (STUDENT/TEACHER)
2. **Login**: User enters email and password
3. **Backend Response**: Returns JWT token, userId, email, and role
4. **Storage**: Token and user data stored in localStorage
5. **Requests**: Axios interceptor automatically adds `Authorization: Bearer {token}` to all requests
6. **Error Handling**: 
   - 401 → Logout and redirect to login
   - 403 → Show permission error
   - 500 → Show server error

## Data Flow Example

### Student Enrolling in a Course
```
1. User clicks "Қатысу" (Enroll) button
2. Frontend calls: enrollmentApi.enrollInCourse(courseId)
3. Axios interceptor adds auth header
4. Backend: POST /api/courses/{courseId}/enroll
5. Backend validates user and creates enrollment
6. Response: { id, courseId, studentId, enrolledAt }
7. Frontend updates UI to show "Сіз қатысқансыз"
```

### Teacher Creating a Lesson
```
1. Teacher fills in lesson title and content
2. Frontend calls: lessonApi.createLesson(courseId, title, content)
3. Backend: POST /api/courses/{courseId}/lessons
4. Backend creates lesson in database
5. Response: { id, courseId, titleKk, contentKk, published, ... }
6. Frontend refreshes lessons list
```

## User Interface Features

### Navigation
- Role-based navbar with links to appropriate dashboards
- Logo and branding
- Logout button for authenticated users
- Responsive menu

### Styling
- **Color Scheme**: Blue primary, green secondary, red danger
- **Font**: System fonts for optimal performance
- **Spacing**: Consistent padding and margins
- **Cards**: Rounded corners with shadows
- **Buttons**: Multiple variants (primary, secondary, danger)
- **Mobile Responsive**: Works on all screen sizes

### User-Friendly Features
- Loading spinners during API calls
- Error messages in Kazakh
- Form validation
- Modal dialogs for actions
- Confirmation before delete
- Progress bars for completion percentage
- Visual indicators for published/draft status

## Kazakh Language Support

All UI text is in Kazakh (Cyrillic):
- "Кіру" = Login
- "Тіркеу" = Register
- "Қатысу" = Enroll
- "Сабақ" = Lesson
- "Тапсырма" = Assignment
- "Оқу" = Read/Study
- "Аяқтау" = Complete
- "Өңдеу" = Edit
- "Өшіру" = Delete
- And many more...

## API Integration

All API calls go through the axios instance in `src/api/axios.ts`:

```typescript
// Example: Get all courses
import { courseApi } from './api/courseApi';
const courses = await courseApi.getAllCourses();

// Example: Login
import { authApi } from './api/authApi';
const response = await authApi.login(email, password);
```

Each API module (`authApi`, `courseApi`, etc.) corresponds to a backend endpoint group.

## Error Handling

The application handles errors gracefully:

```typescript
try {
  const courses = await courseApi.getAllCourses();
  // Use courses
} catch (error) {
  const message = errorUtils.getErrorMessage(error);
  setError(message); // Display to user
}
```

## Building for Production

```bash
npm run build
```

This creates an optimized `dist/` folder ready for deployment. The build:
- Compiles TypeScript
- Bundles all code
- Minifies CSS and JavaScript
- Creates source maps
- Optimizes assets

## Deployment Options

### 1. Static Hosting (Recommended)
- Vercel, Netlify, GitHub Pages
- Upload `dist/` folder
- Set environment variable

### 2. Node.js Server
- Serve `dist/` folder
- Set `VITE_API_BASE_URL` environment variable

### 3. Docker
- Build: `docker build -t qazaq-learn-frontend .`
- Run: `docker run -p 3000:3000 qazaq-learn-frontend`

## Common Tasks

### Add a New Page
1. Create `src/pages/NewPage.tsx`
2. Add route in `App.tsx`
3. Import and use component

### Add a New Component
1. Create `src/components/NewComponent.tsx`
2. Export from component file
3. Import where needed

### Call a New API Endpoint
1. Add method to appropriate API file
2. Use in component with try/catch
3. Display error if failed

### Style with Tailwind
```jsx
<div className="bg-white rounded-lg shadow-lg p-6">
  <h1 className="text-2xl font-bold text-gray-900">Title</h1>
</div>
```

## Troubleshooting

### Port 3000 Already in Use
```bash
# Use different port
npm run dev -- --port 3001
```

### API Connection Issues
- Check backend is running: `http://localhost:8080`
- Verify `.env` has correct `VITE_API_BASE_URL`
- Check browser console for errors

### Build Errors
```bash
# Clear node_modules and reinstall
rm -r node_modules
npm install
npm run build
```

### CORS Issues
- Ensure backend has CORS configured correctly
- Frontend should be able to call backend API

## Performance Tips

1. Use React DevTools profiler
2. Check Network tab in browser
3. Lazy load components if needed
4. Use React.memo for expensive renders
5. Optimize images and assets

## Security Notes

- JWT tokens stored in localStorage (not ideal for highly sensitive data)
- Consider using httpOnly cookies for production
- HTTPS required in production
- Backend should validate all requests
- Frontend should never trust user data

## Next Steps

1. **Install dependencies**: `npm install`
2. **Create .env file**: `copy .env.example .env`
3. **Start server**: `npm run dev`
4. **Test with backend**: Ensure backend is running on port 8080
5. **Create account**: Register as student/teacher
6. **Test features**: Try different user roles

## Support & Questions

Refer to README.md for detailed API documentation and feature descriptions.

Enjoy building with Qazaq Learn! 🚀
