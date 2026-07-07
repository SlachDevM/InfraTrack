package com.infratrack.mobile.dto;

public class MobileWorkOrderAllowedActionsResponse {

    private boolean canCompleteMaintenance;
    private boolean canUploadDocument;
    private boolean canViewAsset;

    public MobileWorkOrderAllowedActionsResponse(
            boolean canCompleteMaintenance,
            boolean canUploadDocument,
            boolean canViewAsset) {
        this.canCompleteMaintenance = canCompleteMaintenance;
        this.canUploadDocument = canUploadDocument;
        this.canViewAsset = canViewAsset;
    }

    public boolean isCanCompleteMaintenance() {
        return canCompleteMaintenance;
    }

    public boolean isCanUploadDocument() {
        return canUploadDocument;
    }

    public boolean isCanViewAsset() {
        return canViewAsset;
    }
}
