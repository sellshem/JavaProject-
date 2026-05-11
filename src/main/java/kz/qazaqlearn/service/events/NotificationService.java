package kz.qazaqlearn.service.events;

import kz.qazaqlearn.domain.events.AssignmentSubmittedEvent;
import kz.qazaqlearn.domain.events.CourseCreatedEvent;
import kz.qazaqlearn.domain.events.CourseEnrollmentEvent;
import kz.qazaqlearn.domain.events.CoursePublishedEvent;
import kz.qazaqlearn.domain.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    // This is a placeholder service for handling events
    // In production, this would integrate with email service, push notifications, etc.
    
    public void sendCourseCreatedNotification(CourseCreatedEvent event) {
        log.info("NOTIFICATION: Course created - ID: {}, Teacher: {}", 
            event.courseId(), event.teacherId());
        // TODO: Send email to teacher with course creation confirmation
    }
    
    public void sendCoursePublishedNotification(CoursePublishedEvent event) {
        log.info("NOTIFICATION: Course published - ID: {}, Published: {}", 
            event.courseId(), event.published());
        // TODO: Send notifications to enrolled students
    }
    
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info("NOTIFICATION: Welcome email sent - User: {}, Email: {}, Role: {}", 
            event.userId(), event.email(), event.role());
        // TODO: Integrate with email service (SendGrid, SES, etc.)
    }
    
    public void sendSubmissionNotification(AssignmentSubmittedEvent event) {
        log.info("NOTIFICATION: Assignment submitted - Submission: {}, Assignment: {}, Student: {}", 
            event.submissionId(), event.assignmentId(), event.studentId());
        // TODO: Notify teacher about new submission
    }
    
    public void sendEnrollmentConfirmation(CourseEnrollmentEvent event) {
        log.info("NOTIFICATION: Enrollment confirmed - ID: {}, Course: {}, Student: {}", 
            event.enrollmentId(), event.courseId(), event.studentId());
        // TODO: Send confirmation to student
    }
}
