package com.infratrack.config;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundException_returns404WithMessage() {
        ResponseEntity<String> response = handler.handleNotFoundException(new NotFoundException("Asset not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Asset not found");
    }

    @Test
    void handleBusinessValidationException_returns400WithMessage() {
        ResponseEntity<String> response = handler.handleBusinessValidationException(
                new BusinessValidationException("Department is required"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Department is required");
    }

    @Test
    void handleConflictException_returns409WithMessage() {
        ResponseEntity<String> response = handler.handleConflictException(
                new ConflictException("Work order has already been assigned"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Work order has already been assigned");
    }

    @Test
    void handleForbiddenOperationException_returns403WithMessage() {
        ResponseEntity<String> response = handler.handleForbiddenOperationException(
                new ForbiddenOperationException("Only managers can register assets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Only managers can register assets");
    }

    @Test
    void handleMethodArgumentNotValidException_returnsFirstFieldErrorMessage() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must be a well-formed email address"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<String> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("email: must be a well-formed email address");
    }

    @Test
    void handleMethodArgumentNotValidException_returnsFallbackWhenNoFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<String> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Validation failed");
    }

    @Test
    void handleDataIntegrityViolationException_returns409WithBusinessMessage() {
        SQLException sqlException = new SQLException(
                "duplicate key value violates unique constraint \"uk_issues_inspection_id\"");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("persist failed", sqlException);

        ResponseEntity<String> response = handler.handleDataIntegrityViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("An issue has already been recorded for this inspection");
    }

    @Test
    void handleDataIntegrityViolationException_returns409WithFallbackForUnknownConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "persist failed",
                new SQLException("duplicate key value violates unique constraint \"uk_unknown\""));

        ResponseEntity<String> response = handler.handleDataIntegrityViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Request conflicts with existing data");
    }
}

