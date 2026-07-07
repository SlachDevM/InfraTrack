package com.infratrack.mobile.sync;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SyncConflictClassifierTest {

    @Test
    void classify_businessValidationException_returnsEmpty() {
        assertThat(SyncConflictClassifier.classify(
                new BusinessValidationException("Question ID is required for each answer")))
                .isEmpty();
    }

    @Test
    void classify_notFound_returnsEntityDeleted() {
        Optional<SyncConflictClassifier.Classification> classification =
                SyncConflictClassifier.classify(new NotFoundException("Inspection not found"));

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.ENTITY_DELETED);
        assertThat(classification.get().message()).isEqualTo("Inspection no longer exists.");
    }

    @Test
    void classify_notFoundForWorkOrder_returnsWorkOrderEntityDeletedMessage() {
        Optional<SyncConflictClassifier.Classification> classification = SyncConflictClassifier.classify(
                new NotFoundException("Work order not found"),
                "WORK_ORDER");

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.ENTITY_DELETED);
        assertThat(classification.get().message()).isEqualTo("Work order no longer exists.");
    }

    @Test
    void classify_completedWorkOrder_returnsWorkflowCompletedMessage() {
        Optional<SyncConflictClassifier.Classification> classification = SyncConflictClassifier.classify(
                new ConflictException("Work order is no longer editable."),
                "WORK_ORDER");

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(classification.get().message()).isEqualTo("Work order is no longer editable.");
    }

    @Test
    void classify_forbidden_returnsPermissionDenied() {
        Optional<SyncConflictClassifier.Classification> classification = SyncConflictClassifier.classify(
                new ForbiddenOperationException("Only the assigned user can save inspection answers"));

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.PERMISSION_DENIED);
    }

    @Test
    void classify_completedInspection_returnsWorkflowCompleted() {
        Optional<SyncConflictClassifier.Classification> classification = SyncConflictClassifier.classify(
                new ConflictException("Inspection progress cannot be modified after completion"));

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(classification.get().message()).isEqualTo("Inspection is no longer editable.");
    }

    @Test
    void classify_duplicateAnswer_returnsVersionMismatch() {
        Optional<SyncConflictClassifier.Classification> classification = SyncConflictClassifier.classify(
                new ConflictException("Duplicate answer submitted for the same checklist question"));

        assertThat(classification).isPresent();
        assertThat(classification.get().conflictType()).isEqualTo(SyncConflictType.VERSION_MISMATCH);
    }
}
