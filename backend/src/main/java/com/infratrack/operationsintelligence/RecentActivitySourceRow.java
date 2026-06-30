package com.infratrack.operationsintelligence;

record RecentActivitySourceRow(
        RecentActivityType type,
        Long assetId,
        String assetName,
        Long occurredAt,
        String description) {
}
