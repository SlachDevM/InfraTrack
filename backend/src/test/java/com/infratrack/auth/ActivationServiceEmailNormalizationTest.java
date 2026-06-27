package com.infratrack.auth;

import com.infratrack.department.DepartmentRepository;
import com.infratrack.mail.EmailService;
import com.infratrack.user.EmailNormalizer;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationServiceEmailNormalizationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AccountActivationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ActivationService activationService;

    @Test
    void createEmployeeInvitation_storesInvitedEmailAsLowercase() {
        User admin = new User("admin@test.com", "password", "Admin", UserRole.ADMINISTRATOR);
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("john.doe@company.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        User created = activationService.createEmployeeInvitation(
                1L,
                "John Worker",
                "John.Doe@Company.com",
                UserRole.FIELD_EMPLOYEE,
                null
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("john.doe@company.com");
        assertThat(created.getEmail()).isEqualTo("john.doe@company.com");
        assertThat(EmailNormalizer.normalize("John.Doe@Company.com")).isEqualTo("john.doe@company.com");
    }

    @Test
    void createEmployeeInvitation_rejectsDuplicateEmailIgnoringCase() {
        User admin = new User("admin@test.com", "password", "Admin", UserRole.ADMINISTRATOR);
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("john@company.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> activationService.createEmployeeInvitation(
                1L,
                "John Worker",
                "JOHN@COMPANY.COM",
                UserRole.FIELD_EMPLOYEE,
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any(User.class));
    }
}
