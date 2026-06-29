package com.infratrack.unitofmeasure.dto;

import com.infratrack.unitofmeasure.QuantityType;
import com.infratrack.unitofmeasure.UnitOfMeasure;

public class UnitOfMeasureResponse {

    private Long id;
    private String code;
    private String symbol;
    private String name;
    private QuantityType quantityType;
    private boolean active;

    public static UnitOfMeasureResponse from(UnitOfMeasure unit) {
        UnitOfMeasureResponse response = new UnitOfMeasureResponse();
        response.id = unit.getId();
        response.code = unit.getCode();
        response.symbol = unit.getSymbol();
        response.name = unit.getName();
        response.quantityType = unit.getQuantityType();
        response.active = unit.isActive();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public QuantityType getQuantityType() {
        return quantityType;
    }

    public boolean isActive() {
        return active;
    }
}
