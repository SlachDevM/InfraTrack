# Developer Guide: Adding Your First Business Module

## Overview

The Business Platform Template provides production-ready infrastructure for authentication, user management, notifications, and deployment. This guide explains how to add your first business module (entity, workflow, API).

The template intentionally contains no business logic. Each application built from it creates its own domain.

---

## Architecture Pattern

The platform follows a simple, proven architecture:

```text
REST Endpoint
    ↓
Controller (request mapping)
    ↓
Service (business logic)
    ↓
Repository (data access)
    ↓
Database Entity
    ↓
PostgreSQL
```

This three-tier pattern (Controller → Service → Repository) is used throughout the platform.

---

## Step 1: Define Your Domain Entity

Create a new JPA entity in `backend/src/main/java/com/mrrg/backend/model/`.

Example: `Project.java`

```java
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}
```

Key principles:

- Use `@Entity` and `@Table` for JPA mapping
- Use `Long` for primary keys
- Use `Instant` for technical timestamps
- Use `LocalDate` for business dates
- Use enums for constrained values
- Add `createdAt` and `updatedAt` for auditability

---

## Step 2: Create a Repository

Create a repository in `backend/src/main/java/com/mrrg/backend/repository/ProjectRepository.java`:

```java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatus(ProjectStatus status);
    Optional<Project> findByIdAndStatus(Long id, ProjectStatus status);
}
```

The repository provides data access methods.

---

## Step 3: Create a Service

Create a service in `backend/src/main/java/com/mrrg/backend/service/ProjectService.java`:

```java
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository repository;
    private final NotificationService notificationService;

    public Project create(String name, String description) {
        // Business validation
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name required");
        }

        // Create entity
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.PENDING);

        // Persist
        Project saved = repository.save(project);

        // Create notification
        notificationService.create(
            currentUser.getId(),
            "New Project Created",
            "Project " + name + " was created"
        );

        return saved;
    }

    public Project update(Long id, String name, String description) {
        Project project = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        // Business validation
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name required");
        }

        // Update
        project.setName(name);
        project.setDescription(description);

        return repository.save(project);
    }

    public void delete(Long id) {
        Project project = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        repository.delete(project);
    }

    public Project getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    public List<Project> list() {
        return repository.findAll();
    }
}
```

The service contains all business logic and validation.

---

## Step 4: Create DTOs

Create request/response DTOs in `backend/src/main/java/com/mrrg/backend/dto/`:

```java
// CreateProjectRequest.java
@Data
public class CreateProjectRequest {
    @NotNull(message = "Project name required")
    private String name;
    private String description;
}

// UpdateProjectRequest.java
@Data
public class UpdateProjectRequest {
    @NotNull(message = "Project name required")
    private String name;
    private String description;
}

// ProjectResponse.java
@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
```

---

## Step 5: Create a Controller

Create a REST controller in `backend/src/main/java/com/mrrg/backend/controller/ProjectController.java`:

```java
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request) {
        Project project = service.create(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapToResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        Project project = service.update(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(mapToResponse(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        Project project = service.getById(id);
        return ResponseEntity.ok(mapToResponse(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list() {
        List<ProjectResponse> projects = service.list()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}
```

The controller handles HTTP requests and delegates to the service.

---

## Step 6: Test Your Service

Create unit tests in `backend/src/test/java/com/mrrg/backend/service/ProjectServiceTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectService service;

    @Test
    void testCreate() {
        // Given
        String name = "Test Project";
        String description = "Test Description";

        Project saved = new Project();
        saved.setId(1L);
        saved.setName(name);
        saved.setDescription(description);
        saved.setStatus(ProjectStatus.PENDING);

        when(repository.save(any())).thenReturn(saved);

        // When
        Project result = service.create(name, description);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        verify(repository).save(any());
    }

    @Test
    void testCreateWithoutName() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> service.create(null, "Description"));
    }
}
```

Test business logic, validation, and error cases.

---

## Step 7: Add React Components

Create a React page in `frontend/src/pages/ProjectsPage.jsx`:

```jsx
import { useEffect, useState } from 'react';
import { apiClient } from '../services/api';

export default function ProjectsPage() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const response = await apiClient.get('/api/projects');
      setProjects(response.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await apiClient.delete(`/api/projects/${id}`);
      fetchProjects();
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h1>Projects</h1>
      <ul>
        {projects.map(project => (
          <li key={project.id}>
            <span>{project.name}</span>
            <button onClick={() => handleDelete(project.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

---

## Step 8: Add Routing

Update `frontend/src/App.jsx` to include your new route:

```jsx
import ProjectsPage from './pages/ProjectsPage';

<Route path="/projects" element={<PrivateRoute><ProjectsPage /></PrivateRoute>} />
```

---

## Key Principles

- **Backend owns business logic** — All validation, permissions, and workflow decisions belong in the service
- **Explicit over clever** — Write clear, readable code
- **Keep it simple** — Do not introduce unnecessary abstractions
- **Test business logic** — Write unit tests for services
- **Use existing patterns** — Follow the Controller → Service → Repository pattern
- **Document workflows** — Explain non-obvious business rules

---

## Notifications

When your business module needs to notify users, use the built-in notification infrastructure:

```java
notificationService.create(
    userId,
    "Notification Title",
    "Notification message"
);
```

Notifications are persisted and delivered through Firebase Cloud Messaging if configured.

---

## Database Migrations

After adding entities, update the database schema:

```bash
cd backend
mvn flyway:migrate  # If using Flyway
# or
mvn liquibase:update  # If using Liquibase
```

For local development, Hibernate DDL is set to `update`, which creates schema automatically.

For production, use a migration tool (Flyway/Liquibase) to ensure schema changes are controlled.

---

## Summary

You now have a complete business module:

1. Entity (database model)
2. Repository (data access)
3. Service (business logic)
4. Controller (REST API)
5. DTOs (request/response)
6. React components (frontend UI)
7. Tests (service tests)

Follow this pattern for each additional business module your application needs.
