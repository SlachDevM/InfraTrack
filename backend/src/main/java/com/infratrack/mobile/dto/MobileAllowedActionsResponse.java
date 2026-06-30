package com.infratrack.mobile.dto;

public class MobileAllowedActionsResponse {

    private boolean canComplete;
    private boolean canUploadDocument;
    private boolean canViewAsset;

    public MobileAllowedActionsResponse(boolean canComplete, boolean canUploadDocument, boolean canViewAsset) {
        this.canComplete = canComplete;
        this.canUploadDocument = canUploadDocument;
        this.canViewAsset = canViewAsset;
    }

    public boolean isCanComplete() {
        return canComplete;
    }

    public boolean isCanUploadDocument() {
        return canUploadDocument;
    }

    public boolean isCanViewAsset() {
        return canViewAsset;
    }
}
