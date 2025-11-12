package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.shared.kafka.event.EmailEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailEventService {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;
    private final AppConfigProperties appConfigProperties;

    public void sendEmailEvent(final EmailEvent emailEvent,
                               final Map<String, Object> eventHeaders) {
        kafkaTemplate.send(new ProducerRecord<>(
                appConfigProperties.getKafka().getNotificationTopic(),
                null,
                System.currentTimeMillis(),
                null,
                emailEvent,
                getEventHeaders(eventHeaders)
        ));
    }
    private List<Header> getEventHeaders(final Map<String, Object> eventHeaders) {
        List<Header> headers = new ArrayList<>();
        if(eventHeaders.isEmpty()) {
            return headers;
        }
        eventHeaders.forEach((key, value) -> headers.add(new RecordHeader(key, value.toString().getBytes(StandardCharsets.UTF_8))));

        return headers;
    }
}
