package com.infratrack.security;

import com.infratrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountStatusServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserAccountStatusService userAccountStatusService;

    @BeforeEach
    void setUp() {
        userAccountStatusService = new UserAccountStatusService(userRepository);
    }

    @Test
    void isEnabled_shouldReturnTrueForEnabledUser() {
        when(userRepository.existsByIdAndEnabledTrue(5L)).thenReturn(true);

        assertThat(userAccountStatusService.isEnabled(5L)).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalseForDisabledUser() {
        when(userRepository.existsByIdAndEnabledTrue(5L)).thenReturn(false);

        assertThat(userAccountStatusService.isEnabled(5L)).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalseForMissingUser() {
        when(userRepository.existsByIdAndEnabledTrue(99L)).thenReturn(false);

        assertThat(userAccountStatusService.isEnabled(99L)).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalseForNullUserId() {
        assertThat(userAccountStatusService.isEnabled(null)).isFalse();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void isEnabled_shouldUseCacheWithinTtl() {
        when(userRepository.existsByIdAndEnabledTrue(5L)).thenReturn(true);

        assertThat(userAccountStatusService.isEnabled(5L)).isTrue();
        assertThat(userAccountStatusService.isEnabled(5L)).isTrue();

        verify(userRepository, times(1)).existsByIdAndEnabledTrue(5L);
    }

    @Test
    void evict_shouldForceDatabaseLookupOnNextCheck() {
        when(userRepository.existsByIdAndEnabledTrue(5L)).thenReturn(true, false);

        assertThat(userAccountStatusService.isEnabled(5L)).isTrue();
        userAccountStatusService.evict(5L);
        assertThat(userAccountStatusService.isEnabled(5L)).isFalse();

        verify(userRepository, times(2)).existsByIdAndEnabledTrue(5L);
    }
}
