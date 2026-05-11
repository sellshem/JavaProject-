package kz.qazaqlearn.repository;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Lesson;
import kz.qazaqlearn.domain.Progress;
import kz.qazaqlearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgressRepository extends JpaRepository<Progress, UUID> {
    List<Progress> findByStudent(User student);
    List<Progress> findByCourse(Course course);
    List<Progress> findByCourseAndStudent(Course course, User student);
    Optional<Progress> findByCourseAndLessonAndStudent(Course course, Lesson lesson, User student);
}
