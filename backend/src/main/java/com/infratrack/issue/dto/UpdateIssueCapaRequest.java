package com.infratrack.issue.dto;

import jakarta.validation.constraints.Size;

/**
 * Updates optional CAPA metadata on an existing issue.
 * All fields are optional; omitted or blank values clear the stored text.
 */
public class UpdateIssueCapaRequest {

    @Size(max = 4000)
    private String rootCause;

    @Size(max = 4000)
    private String correctiveAction;

    @Size(max = 4000)
    private String preventiveAction;

    @Size(max = 4000)
    private String lessonsLearned;

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getPreventiveAction() {
        return preventiveAction;
    }

    public void setPreventiveAction(String preventiveAction) {
        this.preventiveAction = preventiveAction;
    }

    public String getLessonsLearned() {
        return lessonsLearned;
    }

    public void setLessonsLearned(String lessonsLearned) {
        this.lessonsLearned = lessonsLearned;
    }
}
