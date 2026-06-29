package com.twofa.ems.dto;

public record EmployeeRequest(
        String name,
        String email,
        String department,
        String position
) {
}
