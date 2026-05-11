package kz.qazaqlearn.repository;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByPublishedTrue();
    List<Course> findByTeacher(User teacher);
    Optional<Course> findByIdAndPublishedTrue(UUID id);
    Optional<Course> findByIdAndTeacher(UUID id, User teacher);
}
