package com.infratrack.user.dto;

import jakarta.validation.constraints.NotBlank;

public class FcmTokenRequest {
    @NotBlank
    private String token;

    public FcmTokenRequest() {
    }

    public FcmTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
