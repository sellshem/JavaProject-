package kz.qazaqlearn.repository;

import kz.qazaqlearn.domain.Assignment;
import kz.qazaqlearn.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByCourse(Course course);

    List<Assignment> findByCourseAndLessonIsNull(Course course);

    List<Assignment> findByCourseAndLessonIsNotNull(Course course);
}