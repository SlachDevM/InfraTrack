package com.infratrack.bootstrap;

import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@EnableConfigurationProperties(BootstrapAdminProperties.class)
public class InitialAdministratorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(InitialAdministratorBootstrap.class);
    private static final String DEFAULT_UNSAFE_PASSWORD = "change-me";

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public InitialAdministratorBootstrap(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!properties.isEnabled()) {
            return;
        }

        validateConfiguration();

        if (userRepository.existsByRole(UserRole.ADMINISTRATOR)) {
            log.info("Administrator already exists; bootstrap skipped.");
            return;
        }

        User admin = new User(
                properties.getEmail().trim(),
                passwordEncoder.encode(properties.getPassword()),
                properties.getName().trim(),
                UserRole.ADMINISTRATOR
        );
        admin.setEnabled(true);
        admin.setDepartment(null);
        userRepository.save(admin);

        log.info("Initial administrator bootstrap completed for email {}", properties.getEmail().trim());

        if (DEFAULT_UNSAFE_PASSWORD.equals(properties.getPassword()) && isDevProfile()) {
            log.warn(
                    "Bootstrap admin password is still '{}'. Change it after first login.",
                    DEFAULT_UNSAFE_PASSWORD
            );
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getEmail())) {
            throw new IllegalStateException(
                    "app.bootstrap.admin.enabled is true but app.bootstrap.admin.email is not configured"
            );
        }
        if (!StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException(
                    "app.bootstrap.admin.enabled is true but app.bootstrap.admin.password is not configured"
            );
        }
    }

    private boolean isDevProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile)) {
                return true;
            }
        }
        return environment.getActiveProfiles().length == 0;
    }
}
