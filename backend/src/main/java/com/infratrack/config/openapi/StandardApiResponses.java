package com.infratrack.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents common business error responses returned as plain-text messages by {@code GlobalExceptionHandler}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Business validation failed or invalid request body",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(
                responseCode = "401",
                description = "Not authenticated",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(
                responseCode = "403",
                description = "Operation not permitted for the current user",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(
                responseCode = "404",
                description = "Resource not found",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(
                responseCode = "409",
                description = "Conflict with existing business state",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
})
public @interface StandardApiResponses {
}
