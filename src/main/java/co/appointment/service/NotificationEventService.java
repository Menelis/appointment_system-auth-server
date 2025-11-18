package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.shared.kafka.event.EmailEvent;
import co.appointment.shared.util.KafkaUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationEventService {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;
    private final String notificationTopic;

    public NotificationEventService(final KafkaTemplate<String, EmailEvent> kafkaTemplate, final AppConfigProperties appConfigProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationTopic = appConfigProperties.getKafka().getNotificationTopic();
    }
    public void sendEmailEvent(final EmailEvent emailEvent,
                               final Map<String, Object> eventHeaders) {
        KafkaUtils.sendKafkaEvent(kafkaTemplate, notificationTopic, null, emailEvent, eventHeaders);
    }
}
