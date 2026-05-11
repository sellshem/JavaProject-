package kz.qazaqlearn.service.events;

import kz.qazaqlearn.domain.events.AssignmentSubmittedEvent;
import kz.qazaqlearn.domain.events.CourseCreatedEvent;
import kz.qazaqlearn.domain.events.CourseEnrollmentEvent;
import kz.qazaqlearn.domain.events.CoursePublishedEvent;
import kz.qazaqlearn.domain.events.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String COURSE_TOPIC = "course-events";
    private static final String USER_TOPIC = "user-events";
    private static final String ASSIGNMENT_TOPIC = "assignment-events";
    private static final String ENROLLMENT_TOPIC = "enrollment-events";

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCourseCreatedEvent(CourseCreatedEvent event) {
        kafkaTemplate.send(COURSE_TOPIC, event.courseId().toString(), event);
    }

    public void publishCoursePublishedEvent(CoursePublishedEvent event) {
        kafkaTemplate.send(COURSE_TOPIC, event.courseId().toString(), event);
    }

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send(USER_TOPIC, event.userId().toString(), event);
    }

    public void publishAssignmentSubmittedEvent(AssignmentSubmittedEvent event) {
        kafkaTemplate.send(ASSIGNMENT_TOPIC, event.submissionId().toString(), event);
    }

    public void publishCourseEnrollmentEvent(CourseEnrollmentEvent event) {
        kafkaTemplate.send(ENROLLMENT_TOPIC, event.enrollmentId().toString(), event);
    }
}
