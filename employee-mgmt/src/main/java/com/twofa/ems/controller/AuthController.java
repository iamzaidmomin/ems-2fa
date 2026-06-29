package com.twofa.ems.controller;

import com.twofa.ems.dto.AuthStatusResponse;
import com.twofa.ems.dto.SendOtpRequest;
import com.twofa.ems.dto.VerifyOtpRequest;
import com.twofa.ems.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/status")
    public AuthStatusResponse status(HttpSession session) {
        String email = authService.currentEmail(session);
        return new AuthStatusResponse(email != null, email);
    }

    @PostMapping("/send-otp")
    public Map<String, Object> sendOtp(@RequestBody SendOtpRequest request) {
        return authService.sendOtp(request.email());
    }

    @PostMapping("/verify-otp")
    public AuthStatusResponse verifyOtp(@RequestBody VerifyOtpRequest request, HttpSession session) {
        String email = authService.verifyOtp(request, session);
        return new AuthStatusResponse(true, email);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        authService.logout(session);
        return Map.of("message", "Logged out");
    }
}
