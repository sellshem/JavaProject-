package kz.qazaqlearn.repository;

import kz.qazaqlearn.domain.Course;
import kz.qazaqlearn.domain.Enrollment;
import kz.qazaqlearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByCourseAndStudent(Course course, User student);
    List<Enrollment> findByStudent(User student);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByCourseAndStudent(Course course, User student);
}
