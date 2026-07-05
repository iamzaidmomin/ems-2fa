package com.twofa.ems.kafka;

import com.twofa.ems.avro.EmployeeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

    @KafkaListener(topics = "employee-events", groupId = "employee-consumers")
    public void onEmployeeEvent(EmployeeEvent event) {
        log.info("Received EmployeeEvent: id={} email={} name={}", event.getId(), event.getEmail(), event.getName());
        // Add business logic here (audit, downstream writes, etc.)
    }
}
