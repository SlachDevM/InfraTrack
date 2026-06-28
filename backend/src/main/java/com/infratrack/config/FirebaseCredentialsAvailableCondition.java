package com.infratrack.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.File;

public class FirebaseCredentialsAvailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String path = context.getEnvironment().getProperty("firebase.service-account-path", "").trim();
        if (path.isEmpty()) {
            return false;
        }
        return new File(path).isFile();
    }
}
