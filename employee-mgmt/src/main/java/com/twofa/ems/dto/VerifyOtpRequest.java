package com.twofa.ems.dto;

public record VerifyOtpRequest(String email, String otp) {
}
