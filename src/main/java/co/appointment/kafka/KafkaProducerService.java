package co.appointment.kafka;

import co.appointment.shared.infastructure.AbstractKafkaPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

public class KafkaProducerService extends AbstractKafkaPublisher {

    public KafkaProducerService(
            final KafkaTemplate<String, byte[]> kafkaTemplate,
            final JsonMapper jsonMapper) {
        super(kafkaTemplate, jsonMapper);
    }
}
