package com.infratrack.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateAccountRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 12, max = 128, message = "must be between 12 and 128 characters")
    private String password;

    public ActivateAccountRequest() {
    }

    public ActivateAccountRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
