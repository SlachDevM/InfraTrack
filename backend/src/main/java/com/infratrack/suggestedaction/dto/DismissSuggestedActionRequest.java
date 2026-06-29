package com.infratrack.suggestedaction.dto;

import jakarta.validation.constraints.Size;

public class DismissSuggestedActionRequest {

    @Size(max = 2000)
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
