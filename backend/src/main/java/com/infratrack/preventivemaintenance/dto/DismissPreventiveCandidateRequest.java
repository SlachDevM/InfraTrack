package com.infratrack.preventivemaintenance.dto;

import jakarta.validation.constraints.Size;

public class DismissPreventiveCandidateRequest {

    @Size(max = 4000)
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
