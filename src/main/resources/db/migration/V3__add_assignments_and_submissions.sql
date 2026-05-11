CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    lesson_id UUID NULL,
    title_kk VARCHAR(255) NOT NULL,
    description_kk TEXT NOT NULL,
    deadline TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL
);

CREATE TABLE submissions (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL,
    student_id UUID NOT NULL,
    answer_text TEXT NOT NULL,
    grade INTEGER NULL,
    feedback_kk TEXT NULL,
    submitted_at TIMESTAMP NOT NULL,
    graded_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_submission UNIQUE (assignment_id, student_id)
);