package com.infratrack.preventivemaintenance;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(PreventiveSchedulerProperties.class)
public class PreventiveSchedulerConfiguration {
}
