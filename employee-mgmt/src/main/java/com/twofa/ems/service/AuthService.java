package com.twofa.ems.service;

import com.twofa.ems.dto.VerifyOtpRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    public static final String SESSION_EMAIL = "authEmail";

    private final OtpClient otpClient;

    public AuthService(OtpClient otpClient) {
        this.otpClient = otpClient;
    }

    public Map<String, Object> sendOtp(String email) {
        return otpClient.sendOtp(normalizeEmail(email));
    }

    public String verifyOtp(VerifyOtpRequest request, HttpSession session) {
        Map<String, Object> result = otpClient.verifyOtp(
                normalizeEmail(request.email()),
                request.otp().trim()
        );
        Object verified = result.get("verified");
        if (!Boolean.TRUE.equals(verified)) {
            throw new IllegalStateException("OTP verification failed");
        }
        String verifiedEmail = result.get("email").toString();
        session.setAttribute(SESSION_EMAIL, verifiedEmail);
        return verifiedEmail;
    }

    public String currentEmail(HttpSession session) {
        Object email = session.getAttribute(SESSION_EMAIL);
        return email == null ? null : email.toString();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
