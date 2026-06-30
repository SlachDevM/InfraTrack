package com.infratrack.auth;

import com.infratrack.auth.dto.ActivateAccountRequest;
import com.infratrack.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_shouldRejectPasswordShorterThanTwelveCharacters() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("short");
        request.setName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> "password".equals(v.getPropertyPath().toString())
                        && v.getMessage().contains("12"));
    }

    @Test
    void registerRequest_shouldAcceptPasswordWithTwelveCharacters() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("twelvechars!");
        request.setName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> "password".equals(v.getPropertyPath().toString()));
    }

    @Test
    void activateAccountRequest_shouldRejectPasswordShorterThanTwelveCharacters() {
        ActivateAccountRequest request = new ActivateAccountRequest("token", "short");

        Set<ConstraintViolation<ActivateAccountRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> "password".equals(v.getPropertyPath().toString())
                        && v.getMessage().contains("12"));
    }

    @Test
    void activateAccountRequest_shouldAcceptPasswordWithTwelveCharacters() {
        ActivateAccountRequest request = new ActivateAccountRequest("token", "twelvechars!");

        Set<ConstraintViolation<ActivateAccountRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> "password".equals(v.getPropertyPath().toString()));
    }
}
