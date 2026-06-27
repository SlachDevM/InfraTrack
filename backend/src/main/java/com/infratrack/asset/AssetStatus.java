package com.infratrack.asset;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational service status of a registered asset")
public enum AssetStatus {
    ACTIVE,
    LIMITED_SERVICE,
    OUT_OF_SERVICE,
    DECOMMISSIONED
}
