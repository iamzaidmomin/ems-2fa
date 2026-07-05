package com.twofa.ems.service;

import com.twofa.ems.dto.EmployeeRequest;
import com.twofa.ems.model.Employee;
import com.twofa.ems.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final com.twofa.ems.kafka.ProducerService producerService;

    public EmployeeService(EmployeeRepository repository, com.twofa.ems.kafka.ProducerService producerService) {
        this.repository = repository;
        this.producerService = producerService;
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Employee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    public Employee create(EmployeeRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        Employee employee = new Employee();
        apply(employee, request, email);
        Employee saved = repository.save(employee);
        // Publish event to Kafka (Avro serialized)
        try {
            producerService.publishEmployeeEvent(saved.getId(), saved.getName(), saved.getEmail(), saved.getDepartment(), saved.getPosition());
        } catch (Exception e) {
            // Non-fatal: log and continue
            System.err.println("Failed to publish employee event: " + e.getMessage());
        }
        return saved;
    }

    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = findById(id);
        String email = normalizeEmail(request.email());
        if (!employee.getEmail().equals(email) && repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        apply(employee, request, email);
        return repository.save(employee);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        repository.deleteById(id);
    }

    private void apply(Employee employee, EmployeeRequest request, String email) {
        employee.setName(request.name().trim());
        employee.setEmail(email);
        employee.setDepartment(request.department().trim());
        employee.setPosition(request.position().trim());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
