package com.infratrack.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FcmTokenRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void fcmTokenRequest_shouldRejectBlankToken() {
        FcmTokenRequest request = new FcmTokenRequest("   ");

        Set<ConstraintViolation<FcmTokenRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> "token".equals(v.getPropertyPath().toString()));
    }

    @Test
    void fcmTokenRequest_shouldRejectNullToken() {
        FcmTokenRequest request = new FcmTokenRequest();
        request.setToken(null);

        Set<ConstraintViolation<FcmTokenRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> "token".equals(v.getPropertyPath().toString()));
    }

    @Test
    void fcmTokenRequest_shouldAcceptValidToken() {
        FcmTokenRequest request = new FcmTokenRequest("fcm-token-from-firebase-12345");

        Set<ConstraintViolation<FcmTokenRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
