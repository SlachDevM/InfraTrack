package com.infratrack.mobile.sync.dto;

import com.infratrack.mobile.sync.SyncProtocolVersion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SyncResponse {

    @Schema(description = "Mobile sync protocol version; clients must tolerate unknown fields on newer versions")
    private int protocolVersion = SyncProtocolVersion.CURRENT;

    @Schema(description = "Authoritative server time for the sync response")
    private Instant serverTime;

    @Schema(description = "Opaque cursor for the next incremental download; store and resubmit on the following sync")
    private String nextSyncToken;

    @Schema(description = "Incremental download envelope; all sections empty until delta sync is implemented")
    private SyncDeltaResponse delta = SyncDeltaResponse.empty();

    @Schema(description = "Per-operation upload outcomes")
    private List<SyncOperationResponse> operations = new ArrayList<>();

    @Schema(description = "Business conflicts detected during upload processing")
    private List<SyncConflictResponse> conflicts = new ArrayList<>();

    @Schema(description = "Non-blocking sync warnings")
    private List<SyncWarningResponse> warnings = new ArrayList<>();

    @Schema(description = "When true, the client should perform a full re-download instead of incremental sync")
    private boolean requiresFullSync;

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Instant getServerTime() {
        return serverTime;
    }

    public void setServerTime(Instant serverTime) {
        this.serverTime = serverTime;
    }

    public String getNextSyncToken() {
        return nextSyncToken;
    }

    public void setNextSyncToken(String nextSyncToken) {
        this.nextSyncToken = nextSyncToken;
    }

    public SyncDeltaResponse getDelta() {
        return delta;
    }

    public void setDelta(SyncDeltaResponse delta) {
        this.delta = delta != null ? delta : SyncDeltaResponse.empty();
    }

    public List<SyncOperationResponse> getOperations() {
        return operations;
    }

    public void setOperations(List<SyncOperationResponse> operations) {
        this.operations = operations != null ? operations : new ArrayList<>();
    }

    public List<SyncConflictResponse> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<SyncConflictResponse> conflicts) {
        this.conflicts = conflicts != null ? conflicts : new ArrayList<>();
    }

    public List<SyncWarningResponse> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<SyncWarningResponse> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public boolean isRequiresFullSync() {
        return requiresFullSync;
    }

    public void setRequiresFullSync(boolean requiresFullSync) {
        this.requiresFullSync = requiresFullSync;
    }
}
