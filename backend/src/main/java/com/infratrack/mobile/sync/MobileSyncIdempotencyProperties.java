package com.infratrack.mobile.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mobile.sync.idempotency")
class MobileSyncIdempotencyProperties {

    /**
     * Retention period for processed operation records before scheduled cleanup.
     */
    private int retentionDays = 90;

    int getRetentionDays() {
        return retentionDays;
    }

    void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }
}
