package com.infratrack.config;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerPaginationTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessValidationException_shouldReturnBadRequestForInvalidPagination() {
        ResponseEntity<String> response = handler.handleBusinessValidationException(
                new BusinessValidationException("Page index must be greater than or equal to 0."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Page index must be greater than or equal to 0.");
    }
}
