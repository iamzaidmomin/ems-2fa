package com.twofa.ems.kafka;

import com.twofa.ems.avro.EmployeeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Instant;

@Service
public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEmployeeEvent(long id, String name, String email, String department, String position) {
        EmployeeEvent event = EmployeeEvent.newBuilder()
                .setId(id)
                .setName(name)
                .setEmail(email)
                .setDepartment(department)
                .setPosition(position)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        var future = kafkaTemplate.send("employee-events", Long.toString(id), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish EmployeeEvent id={}", id, ex);
            } else if (result != null) {
                try {
                    log.info("Published EmployeeEvent id={} partition={} offset={}", id,
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } catch (Exception e) {
                    log.warn("Published EmployeeEvent id={} but could not read metadata", id, e);
                }
            }
        });
    }
}
