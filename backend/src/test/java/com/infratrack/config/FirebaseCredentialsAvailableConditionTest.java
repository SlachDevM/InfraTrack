package com.infratrack.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseCredentialsAvailableConditionTest {

    private final FirebaseCredentialsAvailableCondition condition = new FirebaseCredentialsAvailableCondition();

    @Test
    void matches_shouldReturnFalseWhenPathIsBlank() {
        assertThat(condition.matches(contextWithProperty("firebase.service-account-path", "   "), null)).isFalse();
    }

    @Test
    void matches_shouldReturnFalseWhenFileDoesNotExist() {
        assertThat(condition.matches(
                contextWithProperty("firebase.service-account-path", "/tmp/does-not-exist-firebase.json"),
                null
        )).isFalse();
    }

    @Test
    void matches_shouldReturnTrueWhenFileExists() throws IOException {
        Path credentialsFile = Files.createTempFile("firebase-test", ".json");
        Files.writeString(credentialsFile, "{}");

        assertThat(condition.matches(
                contextWithProperty("firebase.service-account-path", credentialsFile.toString()),
                null
        )).isTrue();
    }

    private ConditionContext contextWithProperty(String key, String value) {
        Environment environment = new MockEnvironment().withProperty(key, value);
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }
}
