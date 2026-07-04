package com.infratrack.mobile.dto;

/**
 * Backend-generated action flags for the mobile asset context screen (M4-BE1).
 * Android must not infer these; the backend is the single source of truth.
 */
public class AssetContextAllowedActionsResponse {

    private boolean canViewAsset;
    private boolean canViewInspections;
    private boolean canViewIssues;
    private boolean canViewWorkOrders;
    private boolean canCreateInspection;
    private boolean canCreateIssue;

    public AssetContextAllowedActionsResponse(
            boolean canViewAsset,
            boolean canViewInspections,
            boolean canViewIssues,
            boolean canViewWorkOrders,
            boolean canCreateInspection,
            boolean canCreateIssue) {
        this.canViewAsset = canViewAsset;
        this.canViewInspections = canViewInspections;
        this.canViewIssues = canViewIssues;
        this.canViewWorkOrders = canViewWorkOrders;
        this.canCreateInspection = canCreateInspection;
        this.canCreateIssue = canCreateIssue;
    }

    public boolean isCanViewAsset() {
        return canViewAsset;
    }

    public boolean isCanViewInspections() {
        return canViewInspections;
    }

    public boolean isCanViewIssues() {
        return canViewIssues;
    }

    public boolean isCanViewWorkOrders() {
        return canViewWorkOrders;
    }

    public boolean isCanCreateInspection() {
        return canCreateInspection;
    }

    public boolean isCanCreateIssue() {
        return canCreateIssue;
    }
}
