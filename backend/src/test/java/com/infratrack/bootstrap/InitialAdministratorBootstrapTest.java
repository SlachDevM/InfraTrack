package com.infratrack.bootstrap;

import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialAdministratorBootstrapTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    private BootstrapAdminProperties properties;
    private InitialAdministratorBootstrap bootstrap;

    @BeforeEach
    void setUp() {
        properties = new BootstrapAdminProperties();
        bootstrap = new InitialAdministratorBootstrap(
                properties,
                userRepository,
                passwordEncoder,
                environment
        );
    }

    @Test
    void createsAdmin_whenEnabledAndNoAdministratorExists() {
        properties.setEnabled(true);
        properties.setName("Administrator");
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("SecurePass123");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");

        bootstrap.onApplicationReady();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@infratrack.local");
        assertThat(saved.getName()).isEqualTo("Administrator");
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getDepartment()).isNull();
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        verify(passwordEncoder).encode("SecurePass123");
    }

    @Test
    void doesNothing_whenBootstrapDisabled() {
        properties.setEnabled(false);

        bootstrap.onApplicationReady();

        verify(userRepository, never()).existsByRole(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void doesNothing_whenAdministratorAlreadyExists() {
        properties.setEnabled(true);
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("SecurePass123");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(true);

        bootstrap.onApplicationReady();

        verify(userRepository, never()).save(any());
    }

    @Test
    void failsFast_whenEnabledButEmailMissing() {
        properties.setEnabled(true);
        properties.setEmail("   ");
        properties.setPassword("SecurePass123");

        assertThatThrownBy(() -> bootstrap.onApplicationReady())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email is not configured");

        verify(userRepository, never()).save(any());
    }

    @Test
    void failsFast_whenEnabledButPasswordMissing() {
        properties.setEnabled(true);
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("");

        assertThatThrownBy(() -> bootstrap.onApplicationReady())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password is not configured");

        verify(userRepository, never()).save(any());
    }

    @Test
    void storesEncodedPassword_notRawPassword() {
        properties.setEnabled(true);
        properties.setName("Administrator");
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("plain-text-password");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(passwordEncoder.encode("plain-text-password")).thenReturn("$2a$10$encoded");

        bootstrap.onApplicationReady();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("plain-text-password");
    }

    @Test
    void createdAdminIsEnabled() {
        properties.setEnabled(true);
        properties.setName("Administrator");
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("SecurePass123");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");

        bootstrap.onApplicationReady();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEnabled()).isTrue();
    }

    @Test
    void createdAdminHasAdministratorRole() {
        properties.setEnabled(true);
        properties.setName("Administrator");
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("SecurePass123");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");

        bootstrap.onApplicationReady();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.ADMINISTRATOR);
    }

    @Test
    void createdAdminHasNullDepartment() {
        properties.setEnabled(true);
        properties.setName("Administrator");
        properties.setEmail("admin@infratrack.local");
        properties.setPassword("SecurePass123");

        when(userRepository.existsByRole(UserRole.ADMINISTRATOR)).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");

        bootstrap.onApplicationReady();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getDepartment()).isNull();
    }
}
