package kz.qazaqlearn.service.events;

import kz.qazaqlearn.domain.events.AssignmentSubmittedEvent;
import kz.qazaqlearn.domain.events.CourseCreatedEvent;
import kz.qazaqlearn.domain.events.CourseEnrollmentEvent;
import kz.qazaqlearn.domain.events.CoursePublishedEvent;
import kz.qazaqlearn.domain.events.DomainEvent;
import kz.qazaqlearn.domain.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProcessor {

    private final NotificationService notificationService;

    /**
     * Single Kafka listener that processes all domain events.
     * All messages from multiple topics are routed through this method.
     * Events are deserialized as their concrete types by Spring Kafka's JsonDeserializer.
     */
    @KafkaListener(
        topics = {
            "course-events",
            "user-events", 
            "assignment-events",
            "enrollment-events"
        },
        groupId = "qazaq-learn-group"
    )
    public void process(DomainEvent event) {
        log.debug("Processing event: type={}, aggregateId={}", 
            event.getEventType(), event.getAggregateId());
        
        try {
            switch (event.getEventType()) {
                case "COURSE_CREATED" -> handleCourseCreated((CourseCreatedEvent) event);
                case "COURSE_PUBLISHED" -> handleCoursePublished((CoursePublishedEvent) event);
                case "USER_REGISTERED" -> handleUserRegistered((UserRegisteredEvent) event);
                case "ASSIGNMENT_SUBMITTED" -> handleAssignmentSubmitted((AssignmentSubmittedEvent) event);
                case "COURSE_ENROLLMENT" -> handleCourseEnrollment((CourseEnrollmentEvent) event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast event: {}", event.getClass(), e);
        } catch (Exception e) {
            log.error("Error processing event: {}", event, e);
            // Do not throw exception - let the message be committed to avoid redelivery loop
        }
    }

    private void handleCourseCreated(CourseCreatedEvent event) {
        log.info("Handling CourseCreatedEvent: courseId={}, teacherId={}", 
            event.courseId(), event.teacherId());
        notificationService.sendCourseCreatedNotification(event);
    }

    private void handleCoursePublished(CoursePublishedEvent event) {
        log.info("Handling CoursePublishedEvent: courseId={}, published={}", 
            event.courseId(), event.published());
        if (event.published()) {
            notificationService.sendCoursePublishedNotification(event);
        }
    }

    private void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent: userId={}, email={}, role={}", 
            event.userId(), event.email(), event.role());
        notificationService.sendWelcomeEmail(event);
    }

    private void handleAssignmentSubmitted(AssignmentSubmittedEvent event) {
        log.info("Handling AssignmentSubmittedEvent: submissionId={}, assignmentId={}, studentId={}", 
            event.submissionId(), event.assignmentId(), event.studentId());
        notificationService.sendSubmissionNotification(event);
    }

    private void handleCourseEnrollment(CourseEnrollmentEvent event) {
        log.info("Handling CourseEnrollmentEvent: enrollmentId={}, courseId={}, studentId={}", 
            event.enrollmentId(), event.courseId(), event.studentId());
        notificationService.sendEnrollmentConfirmation(event);
    }
}