package com.infratrack.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Architecture guard: protected REST controllers must either depend on an {@code *AuthorizationService}
 * (directly or via a delegated application service) or be explicitly allowlisted with a documented reason.
 * <p>
 * This is a lightweight safety net — not a substitute for endpoint-specific authorization tests.
 */
class AuthorizationArchitectureTest {

    private static final String BASE_PACKAGE = "com.infratrack";

    /**
     * Controllers exempt from the {@code *AuthorizationService} dependency rule.
     * Each entry must document why the controller does not follow the standard pattern.
     */
    private static final Map<String, String> ALLOWLIST = Map.ofEntries(
            Map.entry(
                    "AuthController",
                    "Public authentication endpoints (login, register, activate); JWT not required"),
            Map.entry(
                    "UnitOfMeasureController",
                    "Reference data; readable by all authenticated users"),
            Map.entry(
                    "AssetCategoryController",
                    "Reference data with administrator-only mutations via controller-level UserService checks"),
            Map.entry(
                    "DepartmentController",
                    "Reference data with administrator-only mutations via controller-level UserService checks"),
            Map.entry(
                    "AssetController",
                    "Service-level role and department checks in AssetService via UserService"),
            Map.entry(
                    "BusinessTriggerController",
                    "Service-level role checks in BusinessTriggerService via UserService"),
            Map.entry(
                    "IssueController",
                    "Service-level role and assignment checks in IssueService via UserService"),
            Map.entry(
                    "OperationalDecisionController",
                    "Service-level manager and delegation checks in OperationalDecisionService"),
            Map.entry(
                    "DecisionAssistantController",
                    "Service-level authorization in DecisionAssistantService via UserService"),
            Map.entry(
                    "DelegatedAuthorityController",
                    "Service-level manager checks in DelegatedAuthorityService via UserService"),
            Map.entry(
                    "UserController",
                    "User and role management via UserManagementService and UserService admin checks"));

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    @Test
    void protectedControllersFollowAuthorizationArchitecture() {
        List<ControllerAuthStatus> statuses = discoverControllerStatuses();
        List<String> violations = statuses.stream()
                .filter(status -> status.category() == AuthCategory.UNPROTECTED)
                .map(status -> status.controllerSimpleName()
                        + " has no *AuthorizationService dependency path and is not allowlisted")
                .toList();

        String inventory = buildInventoryReport(statuses);
        assertTrue(
                violations.isEmpty(),
                "Controllers missing authorization architecture:\n"
                        + String.join("\n", violations)
                        + "\n\nController authorization inventory:\n"
                        + inventory
                        + "\n\nAdd *AuthorizationService (controller or delegated service) or document an exemption in "
                        + getClass().getSimpleName());
    }

    @Test
    void controllerAuthorizationInventoryIsComplete() {
        List<ControllerAuthStatus> statuses = discoverControllerStatuses();
        long restControllers = importedClasses.stream()
                .filter(clazz -> clazz.isAnnotatedWith(RestController.class))
                .count();

        assertTrue(
                statuses.size() == restControllers,
                "Expected inventory for every @RestController (" + restControllers + "), found "
                        + statuses.size()
                        + "\n"
                        + buildInventoryReport(statuses));
    }

    private List<ControllerAuthStatus> discoverControllerStatuses() {
        return importedClasses.stream()
                .filter(clazz -> clazz.isAnnotatedWith(RestController.class))
                .map(this::classifyController)
                .sorted(Comparator.comparing(ControllerAuthStatus::controllerSimpleName))
                .toList();
    }

    private ControllerAuthStatus classifyController(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        if (ALLOWLIST.containsKey(simpleName)) {
            return new ControllerAuthStatus(simpleName, AuthCategory.ALLOWLISTED, ALLOWLIST.get(simpleName));
        }

        Class<?> clazz = javaClass.reflect();
        if (hasAuthorizationServiceDependency(clazz)) {
            return new ControllerAuthStatus(
                    simpleName, AuthCategory.CONTROLLER_AUTH, "Direct *AuthorizationService dependency");
        }
        if (hasDelegatedServiceAuth(clazz)) {
            return new ControllerAuthStatus(
                    simpleName,
                    AuthCategory.SERVICE_AUTH,
                    "Delegates to application service with *AuthorizationService");
        }
        return new ControllerAuthStatus(simpleName, AuthCategory.UNPROTECTED, "None");
    }

    private boolean hasAuthorizationServiceDependency(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (isAuthorizationService(field.getType())) {
                return true;
            }
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (isAuthorizationService(parameterType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDelegatedServiceAuth(Class<?> controller) {
        Set<Class<?>> serviceTypes = findInjectedApplicationServiceTypes(controller);
        return serviceTypes.stream().anyMatch(this::typeHasAuthorizationService);
    }

    private Set<Class<?>> findInjectedApplicationServiceTypes(Class<?> controller) {
        return java.util.Arrays.stream(controller.getDeclaredConstructors())
                .flatMap(constructor -> java.util.Arrays.stream(constructor.getParameterTypes()))
                .filter(this::isDelegatedApplicationService)
                .collect(Collectors.toSet());
    }

    private boolean isDelegatedApplicationService(Class<?> type) {
        if (!type.getPackageName().startsWith(BASE_PACKAGE)) {
            return false;
        }
        String simpleName = type.getSimpleName();
        return simpleName.endsWith("Service") && !simpleName.equals("UserService");
    }

    private boolean typeHasAuthorizationService(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (isAuthorizationService(field.getType())) {
                return true;
            }
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (isAuthorizationService(parameterType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAuthorizationService(Class<?> type) {
        return type.getSimpleName().endsWith("AuthorizationService");
    }

    private static String buildInventoryReport(List<ControllerAuthStatus> statuses) {
        Map<AuthCategory, List<ControllerAuthStatus>> grouped = new LinkedHashMap<>();
        for (AuthCategory category : AuthCategory.values()) {
            grouped.put(category, new ArrayList<>());
        }
        statuses.forEach(status -> grouped.get(status.category()).add(status));

        StringBuilder report = new StringBuilder();
        for (Map.Entry<AuthCategory, List<ControllerAuthStatus>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            report.append(entry.getKey().label()).append(":\n");
            entry.getValue().forEach(status -> report.append("  - ")
                    .append(status.controllerSimpleName())
                    .append(" — ")
                    .append(status.detail())
                    .append('\n'));
        }
        return report.toString().trim();
    }

    private enum AuthCategory {
        CONTROLLER_AUTH("Protected by controller-level *AuthorizationService"),
        SERVICE_AUTH("Protected by service-level *AuthorizationService"),
        ALLOWLISTED("Explicitly allowlisted"),
        UNPROTECTED("UNPROTECTED");

        private final String label;

        AuthCategory(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private record ControllerAuthStatus(String controllerSimpleName, AuthCategory category, String detail) {}
}
