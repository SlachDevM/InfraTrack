package com.infratrack.unitofmeasure;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.unitofmeasure.dto.UnitOfMeasureResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/units-of-measure")
@Tag(name = "Units of Measure", description = "Reference units for NUMBER checklist questions (V2 Domain Engine)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService) {
        this.unitOfMeasureService = unitOfMeasureService;
    }

    @GetMapping
    @Operation(summary = "List units of measure", description = "Optional filters: active, quantityType")
    @ApiResponse(responseCode = "200", description = "Unit list")
    public ResponseEntity<List<UnitOfMeasureResponse>> listUnits(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) QuantityType quantityType) {
        return ResponseEntity.ok(unitOfMeasureService.list(active, quantityType));
    }
}
