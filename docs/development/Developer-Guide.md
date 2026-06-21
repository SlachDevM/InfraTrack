# Developer Guide: Adding Your First Business Module

## Overview

The Business Platform Template provides production-ready infrastructure for authentication, user management, notifications, and deployment. This guide explains the process and architecture pattern for adding your first business module (entity, workflow, API).

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

File name: `YourBusinessEntity.java`

**Key principles:**

- Use `@Entity` and `@Table` for JPA mapping
- Use `Long` for primary keys
- Use `Instant` for technical timestamps (createdAt, updatedAt)
- Use `LocalDate` for business dates
- Use enums for constrained values
- Add `createdAt` and `updatedAt` for auditability

**Example structure:**

```java
@Entity
@Table(name = "your_business_entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourBusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private YourBusinessStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}
```

Also define your status enum:

```java
public enum YourBusinessStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}
```

---

## Step 2: Create a Repository

Create a repository in `backend/src/main/java/com/mrrg/backend/repository/YourBusinessRepository.java`:

The repository provides data access methods using Spring Data JPA.

**Key responsibilities:**

- Extend `JpaRepository<YourBusinessEntity, Long>`
- Add finder methods for common queries
- Keep queries simple and declarative

**Example structure:**

```java
@Repository
public interface YourBusinessRepository extends JpaRepository<YourBusinessEntity, Long> {
    // Add custom finder methods as needed
    List<YourBusinessEntity> findByStatus(YourBusinessStatus status);
}
```

---

## Step 3: Create a Service

Create a service in `backend/src/main/java/com/mrrg/backend/service/YourBusinessService.java`:

**Key responsibilities:**

- All business logic and validation
- Transaction management
- Calling repositories for data access
- Creating notifications via `notificationService`
- Throwing appropriate exceptions for error cases

**Example structure:**

```java
@Service
@RequiredArgsConstructor
public class YourBusinessService {
    private final YourBusinessRepository repository;
    private final NotificationService notificationService;

    public YourBusinessEntity create(String name, String description) {
        // Business validation
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name required");
        }

        // Create entity
        YourBusinessEntity entity = new YourBusinessEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setStatus(YourBusinessStatus.PENDING);

        // Persist
        YourBusinessEntity saved = repository.save(entity);

        // Optionally notify
        notificationService.create(
            getCurrentUserId(),
            "Entity Created",
            "New entity " + name + " was created"
        );

        return saved;
    }

    public YourBusinessEntity getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found"));
    }

    public List<YourBusinessEntity> list() {
        return repository.findAll();
    }
}
```

---

## Step 4: Create DTOs

Create request/response DTOs in `backend/src/main/java/com/mrrg/backend/dto/`:

**Key principles:**

- Request DTOs use `@Valid` and `@NotNull` for validation
- Response DTOs map entity data for API clients
- DTOs decouple API contract from entity model

**Example structure:**

```java
// CreateYourBusinessRequest.java
@Data
public class CreateYourBusinessRequest {
    @NotNull(message = "Name required")
    private String name;
    private String description;
}

// YourBusinessResponse.java
@Data
public class YourBusinessResponse {
    private Long id;
    private String name;
    private String description;
    private YourBusinessStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
```

---

## Step 5: Create a Controller

Create a REST controller in `backend/src/main/java/com/mrrg/backend/controller/YourBusinessController.java`:

**Key responsibilities:**

- Map HTTP requests to service methods
- Handle validation errors
- Return appropriate HTTP status codes
- Map entities to response DTOs

**Example structure:**

```java
@RestController
@RequestMapping("/api/yourbusiness")
@RequiredArgsConstructor
public class YourBusinessController {
    private final YourBusinessService service;

    @PostMapping
    public ResponseEntity<YourBusinessResponse> create(
            @Valid @RequestBody CreateYourBusinessRequest request) {
        YourBusinessEntity entity = service.create(
            request.getName(),
            request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapToResponse(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<YourBusinessResponse> getById(@PathVariable Long id) {
        YourBusinessEntity entity = service.getById(id);
        return ResponseEntity.ok(mapToResponse(entity));
    }

    @GetMapping
    public ResponseEntity<List<YourBusinessResponse>> list() {
        List<YourBusinessResponse> responses = service.list()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private YourBusinessResponse mapToResponse(YourBusinessEntity entity) {
        YourBusinessResponse response = new YourBusinessResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
```

---

## Step 6: Test Your Service

Create unit tests in `backend/src/test/java/com/mrrg/backend/service/YourBusinessServiceTest.java`:

**Key principles:**

- Test business logic, not framework behavior
- Use mocks for dependencies (repository, notificationService)
- Test validation logic
- Test error cases

**Example structure:**

```java
@ExtendWith(MockitoExtension.class)
class YourBusinessServiceTest {
    @Mock
    private YourBusinessRepository repository;

    @InjectMocks
    private YourBusinessService service;

    @Test
    void testCreate() {
        // Given
        String name = "Test Entity";

        YourBusinessEntity saved = new YourBusinessEntity();
        saved.setId(1L);
        saved.setName(name);
        saved.setStatus(YourBusinessStatus.PENDING);

        when(repository.save(any())).thenReturn(saved);

        // When
        YourBusinessEntity result = service.create(name, "description");

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        verify(repository).save(any());
    }

    @Test
    void testCreateWithoutName() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> service.create(null, "description"));
    }
}
```

---

## Step 7: Add React API Service

Create an API service in `frontend/src/services/yourBusinessApi.js`:

**Key responsibilities:**

- Encapsulate API calls to your backend
- Handle authentication (JWT is automatic via apiClient)
- Manage data fetching and error handling

**Example structure:**

```javascript
import { apiClient } from './api';

export const yourBusinessApi = {
  async create(data) {
    const response = await apiClient.post('/api/yourbusiness', data);
    return response.data;
  },

  async getAll() {
    const response = await apiClient.get('/api/yourbusiness');
    return response.data;
  },

  async getById(id) {
    const response = await apiClient.get(`/api/yourbusiness/${id}`);
    return response.data;
  }
};
```

---

## Step 8: Add React Component

Create a React page in `frontend/src/pages/YourBusinessPage.jsx`:

**Key principles:**

- React components handle UI and state
- Call API service for backend data
- Display data and handle user interactions

**Example structure:**

```jsx
import { useEffect, useState } from 'react';
import { yourBusinessApi } from '../services/yourBusinessApi';

export default function YourBusinessPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      const data = await yourBusinessApi.getAll();
      setItems(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h1>Your Business Module</h1>
      <ul>
        {items.map(item => (
          <li key={item.id}>{item.name}</li>
        ))}
      </ul>
    </div>
  );
}
```

---

## Step 9: Add Routing

Update `frontend/src/App.jsx` to include your new route:

```jsx
import YourBusinessPage from './pages/YourBusinessPage';

<Route 
  path="/yourbusiness" 
  element={<PrivateRoute><YourBusinessPage /></PrivateRoute>} 
/>
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

## Notifications (Optional)

When your business module needs to notify users, use the built-in notification infrastructure:

```java
notificationService.create(
    userId,
    "Notification Title",
    "Notification message"
);
```

Notifications are persisted to the database and delivered through Firebase Cloud Messaging if configured.

---

## Database Migrations

After adding entities, update the database schema.

For local development, Hibernate DDL is set to `update`, which creates schema automatically.

For production, use a migration tool:

```bash
cd backend
mvn flyway:migrate  # If using Flyway
# or
mvn liquibase:update  # If using Liquibase
```

---

## Summary

You now understand the structure for adding a business module to the platform:

1. **Entity** — JPA model in `backend/model/`
2. **Repository** — Data access interface
3. **Service** — Business logic and validation
4. **DTOs** — Request/response contracts
5. **Controller** — REST API endpoints
6. **API Service** — React data layer in `frontend/services/`
7. **Component** — React UI page in `frontend/pages/`
8. **Routing** — Register route in `App.jsx`

Follow this pattern for each additional business module your application needs.

Replace `YourBusinessEntity`, `YourBusinessService`, etc. with your actual domain names.

The template provides the infrastructure. Your business logic implements the patterns.
