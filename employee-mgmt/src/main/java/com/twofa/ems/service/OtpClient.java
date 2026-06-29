package com.twofa.ems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.util.Map;

@Service
public class OtpClient {

    private final RestClient restClient;

    public OtpClient(@Value("${twofa.base-url}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public Map<String, Object> sendOtp(String email) {
        try {
            return restClient.post()
                    .uri("/otp/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            throw toException(ex);
        }
    }

    public Map<String, Object> verifyOtp(String email, String otp) {
        try {
            return restClient.post()
                    .uri("/otp/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "otp", otp))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            throw toException(ex);
        }
    }

    private ResponseStatusException toException(RestClientResponseException ex) {
        String detail = "OTP service error";
        try {
            Map<?, ?> body = ex.getResponseBodyAs(Map.class);
            if (body != null && body.get("detail") != null) {
                detail = body.get("detail").toString();
            }
        } catch (Exception ignored) {
            // use default message
        }
        return new ResponseStatusException(HttpStatus.valueOf(ex.getStatusCode().value()), detail);
    }
}
