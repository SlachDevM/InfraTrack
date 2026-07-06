package com.infratrack.mobile.sync;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MobileSyncIdempotencyProperties.class)
class MobileSyncIdempotencyConfiguration {
}
