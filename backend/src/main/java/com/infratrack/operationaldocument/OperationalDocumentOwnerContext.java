package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;

public record OperationalDocumentOwnerContext(
        Asset asset,
        OperationalDocumentOwnerType ownerType,
        Long ownerId) {
}
