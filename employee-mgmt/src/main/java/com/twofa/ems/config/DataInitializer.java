package com.twofa.ems.config;

import com.twofa.ems.model.Employee;
import com.twofa.ems.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedEmployees(EmployeeRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(sample("Alice Johnson", "alice@company.com", "Engineering", "Software Engineer"));
            repository.save(sample("Bob Smith", "bob@company.com", "HR", "HR Manager"));
            repository.save(sample("Carol Lee", "carol@company.com", "Finance", "Accountant"));
        };
    }

    private Employee sample(String name, String email, String department, String position) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setPosition(position);
        return employee;
    }
}
