package com.twofa.ems.dto;

public record AuthStatusResponse(boolean authenticated, String email) {
}
