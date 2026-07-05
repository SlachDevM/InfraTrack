package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class SyncRequest {

    @NotBlank
    @Schema(description = "Stable client installation identifier", example = "android-install-uuid")
    private String clientId;

    @NotBlank
    @Schema(description = "Client sync protocol version", example = "1")
    private String clientVersion;

    @Schema(description = "Mobile app version string", example = "1.1.0")
    private String appVersion;

    @Schema(description = "Opaque sync cursor from the previous successful sync response")
    private String syncToken;

    @Schema(description = "Client clock at sync request time (epoch millis); optional")
    private Long deviceTime;

    @Valid
    @Schema(description = "Queued pending mutations; accepted structurally but not processed in M5.2-BE1")
    private List<PendingOperationRequest> pendingOperations = new ArrayList<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSyncToken() {
        return syncToken;
    }

    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    public Long getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(Long deviceTime) {
        this.deviceTime = deviceTime;
    }

    public List<PendingOperationRequest> getPendingOperations() {
        return pendingOperations;
    }

    public void setPendingOperations(List<PendingOperationRequest> pendingOperations) {
        this.pendingOperations = pendingOperations != null ? pendingOperations : new ArrayList<>();
    }
}
