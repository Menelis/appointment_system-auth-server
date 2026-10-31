package co.appointment.kafka;

import co.appointment.shared.infastructure.AbstractKafkaPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

public class KafkaProducerService extends AbstractKafkaPublisher {

    public KafkaProducerService(KafkaTemplate<String, byte[]> kafkaTemplate, JsonMapper jsonMapper) {
        super(kafkaTemplate, jsonMapper);
    }
}
