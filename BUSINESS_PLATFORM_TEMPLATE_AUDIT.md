# Business Platform Template Extraction Audit — Web Project

**Audit Date:** June 21, 2026  
**Project:** MRRG (Margaret River Re-Gutter)  
**Scope:** Spring Boot backend, React frontend, Docker/configuration, documentation  
**Audit Type:** Read-only assessment (no modifications made)

---

## Executive Summary

The MRRG web project is **exceptionally well-suited** as a source for a Business Platform Template. It demonstrates production-grade architecture, clear separation of concerns, comprehensive authentication/authorization, and a proven ability to support multiple clients with shared business logic.

**Key Strengths:**
- Clean layered architecture (Controller → Service → Repository)
- Robust authentication and account lifecycle management
- Generic platform foundation already extracted
- Explicit, maintainable code aligned with stated project philosophy
- Production-ready Docker configuration with dev/prod separation
- Comprehensive documentation structure
- Well-organized test coverage for core services

**Extraction Feasibility:** HIGH. The codebase is already organized to minimize MRRG-specific domain logic. Most extraction work involves removing business workflows (jobs, scheduling, callbacks, photos) while preserving the proven platform infrastructure.

**Recommended Approach:** Execute extraction in 7-8 phased commits, starting with branding/terminology removal, then progressively eliminating business domain code, generalizing platform components, and finally replacing documentation.

---

## Backend Analysis

### Keep as Generic Platform Foundation

#### Authentication & Security (KEEP - Generic)
- **JwtTokenProvider** (`JwtAuthenticationToken.java`, `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`)
  - Stateless JWT-based authentication
  - Generic implementation suitable for any business platform
  - Token generation, validation, and extraction are business-agnostic
  
- **SecurityConfig** (`SecurityConfig.java`)
  - Spring Security configuration
  - CORS handling with environment-based origin configuration
  - Stateless session policy
  - OpenAPI/Swagger endpoint permissions
  - Generic and reusable

#### User Management Lifecycle (KEEP - Generic)
- **User entity** (`User.java`)
  - Core user fields: email, password, name, role, timestamps
  - Account activation tracking via `enabled` flag and `hasActivatedAccount()` method
  - FCM token storage for notifications
  - Fully generic domain model
  
- **UserRole enum** (`UserRole.java`)
  - EMPLOYEE, MANAGER, ADMIN roles
  - Simple, effective role hierarchy
  - Can be directly adopted by template

- **UserStatus enum** (`UserStatus.java`)
  - PENDING_ACTIVATION, ACTIVE, DISABLED states
  - Precise representation of user account lifecycle
  - Business-agnostic

- **AccountActivationToken entity** (`AccountActivationToken.java`)
  - Secure token generation and expiration handling
  - Email-based activation flow
  - Fully generic

#### Services (KEEP - Generic Core, Generalize Some)
- **AuthService** (`AuthService.java`)
  - Login with email/password validation
  - Account activation with secure tokens
  - Generic authentication workflow
  - KEEP AS-IS

- **ActivationService** (`ActivationService.java`)
  - Token validation and user activation
  - Password setting
  - Generic activation flow
  - KEEP AS-IS

- **UserService** (`UserService.java`)
  - User lookup, role checking, authorization helpers
  - Generic utility service
  - KEEP AS-IS

- **UserManagementService** (`UserManagementService.java`)
  - User invitation and lifecycle management
  - Account deactivation, reactivation, password resets
  - Email change notifications
  - Generic admin operations
  - Well-tested, production-grade
  - KEEP AS-IS

#### Email Service (KEEP - Generic with Branding Removal)
- **EmailService** (`EmailService.java`)
  - SMTP integration with dev/prod profiles
  - Account activation email generation
  - Email change notification emails
  - Development mode logs activation links instead of sending
  - Generic structure with MRRG-specific strings to replace
  - ACTION: Replace "MRRG" branding in email bodies and subjects

#### Notifications & Firebase (KEEP - Generic)
- **Notification entity** (`Notification.java`)
  - Notification persistence and status tracking
  - Generic notification model
  - KEEP AS-IS

- **NotificationType enum** (`NotificationType.java`)
  - Contains MRRG-specific types: JOB_ASSIGNED, JOB_RESCHEDULED, JOB_READY_FOR_CONFIRMATION, JOB_CONFIRMED
  - ACTION: Generalize to EVENT_ASSIGNED, EVENT_SCHEDULED, EVENT_READY_FOR_ACTION, EVENT_CONFIRMED (or similar)

- **NotificationService** (`NotificationService.java`)
  - Notification CRUD and Firebase integration
  - Generic notification orchestration
  - KEEP AS-IS with generalized NotificationType references

- **FirebaseNotificationService** (`FirebaseNotificationService.java`)
  - Firebase Cloud Messaging integration
  - Generic FCM push notification delivery
  - KEEP AS-IS

#### Repositories (KEEP - Generic)
- **UserRepository**
- **AccountActivationTokenRepository**
- **NotificationRepository**
- All are standard Spring Data JPA repositories with generic queries
- KEEP AS-IS

#### Controllers (KEEP - Auth/User/Notification, REMOVE - Jobs)
- **AuthController** (`AuthController.java`)
  - Login, registration, account activation endpoints
  - Generic authentication endpoints
  - KEEP AS-IS

- **UserController** (`UserController.java`)
  - User management endpoints (list, create, update, deactivate, reactivate, resend activation)
  - Generic user lifecycle management
  - KEEP AS-IS with potential DTO generalization

- **NotificationController** (`NotificationController.java`)
  - Notification CRUD and FCM token registration
  - Generic notification endpoints
  - KEEP AS-IS

- **JobController** (`JobController.java`)
  - MRRG-specific job management (create, update, assign, complete, confirm, archive, callback-fix)
  - Contains roofing/guttering business domain logic
  - ACTION: REMOVE entirely (part of domain-specific elimination)

#### DTOs (KEEP - Auth/User, GENERALIZE - Notifications, REMOVE - Jobs)
**Keep As-Is:**
- `LoginRequest.java`, `LoginResponse.java` - Generic auth
- `RegisterRequest.java` - Generic registration
- `ActivateAccountRequest.java` - Generic activation
- `UpdateUserRequest.java` - Generic user updates
- `UserProfileResponse.java` - Generic user profile
- `UserManagementResponse.java` - Generic user admin response
- `FcmTokenRequest.java` - Generic FCM token registration
- `UserSummary.java` - Generic user summary

**Generalize:**
- `CreateEmployeeRequest.java` - Rename to `CreateUserRequest.java` with generic fields

**Remove:**
- `AssignWorkersRequest.java` - MRRG-specific job worker assignment
- `CallbackFixRequest.java` - MRRG-specific callback workflow

#### Configuration (KEEP - Generic)
- **SecurityConfig** - KEEP AS-IS (covered above)
- **FirebaseConfig** (`FirebaseConfig.java`) - KEEP AS-IS (generic Firebase setup)
- **GlobalExceptionHandler** (`GlobalExceptionHandler.java`) - KEEP AS-IS (generic error handling)
- **OpenApiConfig** (`OpenApiConfig.java`) - KEEP AS-IS (Swagger documentation)
- **DatabaseSchemaUpdater** (`DatabaseSchemaUpdater.java`) - KEEP AS-IS (schema versioning pattern)

#### Application Properties (GENERALIZE - Remove MRRG References)
- **application.properties** (`application.properties`)
  - Database configuration: KEEP (uses env vars)
  - Mail configuration: KEEP (generic SMTP)
  - Firebase configuration: KEEP (generic)
  - Account activation: KEEP (generic)
  - CORS configuration: KEEP (uses env vars)
  - JWT configuration: KEEP (generic)
  - ACTION: Update default values and documentation comments

- **application-prod.properties** (`application-prod.properties`)
  - Production-specific profile
  - Generic settings
  - KEEP AS-IS

#### POM.xml (KEEP - Generic)
- Dependency list is production-standard and domain-agnostic
- Spring Boot 3.2.7, Java 21, PostgreSQL, JWT, Firebase Admin, Mail, etc.
- KEEP AS-IS

### Generalize (Rename/Refactor)

#### Backend Generalization Tasks

1. **NotificationType enum** — Rename notification types from job-specific to generic event types
   - JOB_ASSIGNED → ENTITY_ASSIGNED
   - JOB_RESCHEDULED → ENTITY_RESCHEDULED
   - JOB_READY_FOR_CONFIRMATION → ENTITY_READY_FOR_CONFIRMATION
   - JOB_CONFIRMED → ENTITY_CONFIRMED

2. **EmailService** — Generalize email templates
   - Replace "MRRG" with template variable (e.g., "{APP_NAME}")
   - Replace "Welcome to MRRG" with generic activation message
   - Replace "MRRG Team" with "{APP_NAME} Team"

3. **ApplicationProperties** — Update comments and defaults
   - Replace MRRG-specific examples with generic ones
   - Clarify that activation link is customizable (web vs. deep link)

4. **Package Structure** — Keep as-is
   - `com.mrrg.backend` → Should remain as template establishes naming convention
   - Or optionally rename to `com.template.backend` in template version

### Remove from Template

#### Entirely Remove These Components

1. **Job Domain Model & Business Logic**
   - `Job.java` entity (contains clientName, clientPhone, clientAddress, jobTypes, job scheduling fields)
   - `JobStatus.java` enum (PENDING, SCHEDULED, IN_PROGRESS, READY_FOR_CONFIRMATION, DONE, TO_BE_FIXED, ARCHIVED)
   - `JobType.java` enum (roofing/guttering specific)
   - `StringListJsonConverter.java` (custom converter for job photos stored as JSON)

2. **Job Service & Repository**
   - `JobService.java` - Contains complex job workflow with photo management, worker assignment, callback logic
   - `JobRepository.java` - Job database queries
   - `JobController.java` - Job REST endpoints

3. **Job-Related DTOs**
   - `AssignWorkersRequest.java` - Worker assignment
   - `CallbackFixRequest.java` - Callback/rework workflow

4. **Job-Related Tests**
   - `JobServiceTest.java` - Job workflow tests
   - Tests should be replaced with template entity tests

5. **MRRG-Specific Configuration**
   - No configurations are MRRG-specific at the code level
   - Properties are externalized and generic

#### Why These Are Safe to Remove
- Job logic is completely isolated in separate service/controller
- No other backend components depend on Job entity
- User, Auth, Notification systems have zero dependencies on Job
- Job workflow is business-specific, not platform infrastructure

### Placeholder/Example

1. **Sample Business Entity** — Create a generic placeholder
   - Replace Job entity with a `Project.java` or `Task.java` template entity
   - Include same patterns: JPA annotations, timestamps, relationship patterns
   - Document how to extend for specific business domains

2. **Sample Service** — Create a template service pattern
   - Replace JobService with `ProjectService.java` or `TaskService.java`
   - Show standard CRUD patterns with business rule enforcement
   - Include notification integration example

3. **Sample Controller** — Create a template REST controller
   - Show standard REST patterns (GET, POST, PUT, DELETE)
   - Include authentication and authorization checks
   - Document security best practices

### Manual Decisions Required

1. **Package Naming Convention**
   - Current: `com.mrrg.backend`
   - Decision: Keep as-is or rename to `com.businessplatform.backend` or `com.template.backend`?
   - Recommendation: Create a find-and-replace guide for template consumers

2. **Notification Type Names**
   - Current notification types are job-specific
   - Decision: Rename to generic event types or keep as example and let consumers customize?
   - Recommendation: Generalize to EVENT-based names to show flexibility

3. **Default Configuration Values**
   - Current defaults reference localhost development environment
   - Decision: Keep localhost defaults or provide production-grade examples?
   - Recommendation: Keep localhost for dev convenience, document production setup

4. **Test Coverage Strategy**
   - Current tests are job-workflow focused
   - Decision: Replace with generic user/auth/notification tests or keep as reference?
   - Recommendation: Replace with comprehensive template tests, keep none of the job tests

---

## React Frontend Analysis

### Keep as Generic Platform Foundation

#### Routing & Navigation (KEEP - Generic)
- **App.jsx** (`App.jsx`)
  - BrowserRouter setup with PrivateRoute protection
  - Route structure: /login, /, /admin, /notifications, /users
  - Generic routing pattern
  - KEEP AS-IS

#### Authentication Context (KEEP - Generic)
- **AuthContext** (`AuthContext.jsx`)
  - JWT token and user object in localStorage
  - Login/logout state management
  - Session persistence
  - Fully generic
  - KEEP AS-IS

#### Notification Context (KEEP - Generic)
- **NotificationContext** (`NotificationContext.jsx`)
  - Global notification state management
  - Unread count tracking
  - Generic notification orchestration
  - KEEP AS-IS

#### Services & API Layer (KEEP - Generic)
- **apiClient** (`apiClient.js`)
  - HTTP client with JWT token injection
  - Error handling and token management
  - Generic API abstraction layer
  - KEEP AS-IS

- **userApi** (`userApi.js`)
  - User management endpoints (list, invite, update, deactivate, reactivate, resend activation)
  - Generic user API wrapper
  - KEEP AS-IS

- **jobApi** (`jobApi.js`)
  - Job management endpoints
  - MRRG-specific business domain
  - ACTION: REMOVE entirely

#### Configuration (KEEP - Generic)
- **apiConfig.js** (`apiConfig.js`)
  - Environment-based API URL configuration
  - Generic configuration pattern
  - KEEP AS-IS

- **vite.config.js** (`vite.config.js`)
  - Vite build configuration
  - API proxy for development
  - Generic build setup
  - KEEP AS-IS

#### Components (KEEP - Generic Platform, REMOVE - MRRG-Specific)

**Keep - Generic Platform Components:**
- **NotificationButton** (`NotificationButton.jsx`)
  - Notification bell icon with unread count
  - Generic notification UI component
  - KEEP AS-IS

- **ConfirmDialog** (`ConfirmDialog.jsx`)
  - Reusable confirmation dialog component
  - Generic utility component
  - KEEP AS-IS

- **InviteUserModal** (`InviteUserModal.jsx`)
  - User invitation form
  - Generic admin operation
  - KEEP AS-IS

- **EditUserModal** (`EditUserModal.jsx`)
  - User edit form
  - Generic admin operation
  - KEEP AS-IS

**Remove - MRRG-Specific Business Components:**
- **JobList** (`JobList.jsx`) - Job listing UI specific to roofing business
- **JobCard** (`JobCard.jsx`) - Individual job card display
- **JobModal** (`JobModal.jsx`) - Job creation/editing form with photos, scheduling, worker assignment
- **WeekView** (`WeekView.jsx`) - Week calendar view for job scheduling

#### Pages/Routes (KEEP - Generic Platform, REMOVE - MRRG-Specific)

**Keep - Generic Platform Pages:**
- **Login** (`Login.jsx`)
  - Generic login form with email/password
  - MRRG branding in header image and form text
  - ACTION: Keep logic, replace branding/labels

- **NotificationPage** (`NotificationPage.jsx`)
  - Generic notification inbox view
  - Lists notifications with read/unread status
  - KEEP AS-IS with label generalization

- **UserManagementPage** (`UserManagementPage.jsx`)
  - Generic user administration interface
  - User creation, editing, deactivation, reactivation
  - Search and filter functionality
  - KEEP AS-IS

- **AdminPage** (`AdminPage.jsx`)
  - Dashboard for administrators
  - MRRG-specific: Shows done/archived jobs
  - ACTION: Generalize to show generic entity management (done/archived projects, tasks, etc.)

**Remove - MRRG-Specific Business Pages:**
- **MainDashboard** (`MainDashboard.jsx`) - Job scheduling dashboard specific to roofing work
  - Job list, week view scheduling, job details
  - Worker-specific job filtering
  - Photo upload and job lifecycle tracking

#### Styling (KEEP - Generic Structure, REPLACE - Branding Colors)
- All CSS files are generic component styling
- May reference MRRG-specific colors/branding
- ACTION: Update CSS variable colors and references to generic professional palette

### Generalize (Rename/Refactor)

1. **Login Page Branding**
   - Current: MRRG/RE-GUTTERS logo and branding
   - Replace with template placeholder or generic company logo
   - Update welcome text and form labels

2. **AdminPage** — Generalize from job-specific dashboard
   - Rename to `EntityManagementPage.jsx` or `PlatformDashboardPage.jsx`
   - Remove job-specific terminology (done/archived jobs)
   - Show generic entity states instead

3. **NotificationPage** — Already generic, check labels
   - Ensure notification titles are generic ("Entity Assigned", "Entity Ready", etc.)

4. **API Constants** — Update endpoint naming
   - Current: Job-specific endpoints in jobConfig.js
   - Generalize to generic entity endpoints

### Remove from Template

1. **Job-Related Components (Complete Removal)**
   - JobList.jsx
   - JobCard.jsx
   - JobModal.jsx
   - WeekView.jsx
   - All associated CSS files

2. **Job-Related Services**
   - jobApi.js
   - Job-related API constants

3. **MainDashboard Page**
   - Entire job scheduling dashboard
   - Replace with generic admin dashboard showing users, system health, etc.

4. **Job-Related Styling**
   - JobList.css
   - JobModal.css (shared but heavily used by jobs)
   - WeekView.css
   - Dashboard.css (job dashboard specific)

5. **Job-Related Constants**
   - Job status constants
   - Job type constants
   - Job configuration constants

### Placeholder/Example

1. **Sample Entity Service**
   - Create `projectApi.js` as template example
   - Show CRUD patterns for a generic business entity
   - Document how to adapt for specific domains

2. **Sample Entity Component**
   - Create `ProjectCard.jsx` or `TaskCard.jsx` as template example
   - Show component patterns for displaying business entities
   - Include reusable patterns for list, edit, delete actions

3. **Sample Entity Page**
   - Create `EntityManagementPage.jsx` as template replacement for MainDashboard
   - Show generic CRUD UI pattern
   - Document customization points

### Manual Decisions Required

1. **Admin Dashboard Direction**
   - Current AdminPage shows job archive/completion workflow
   - Decision: Replace with generic admin dashboard (users, system stats) or keep as entity example?
   - Recommendation: Create generic AdminPage that shows system overview

2. **Navigation Labels**
   - Current: Dashboard, Admin, Notifications, Users
   - Decision: Keep generic labels or customize?
   - Recommendation: Keep generic labels, document customization in README

3. **Branding Replacement Strategy**
   - Current: MRRG logo and colors throughout
   - Decision: Use placeholder logo or generic theme?
   - Recommendation: Create a branding configuration file for easy customization

4. **Authentication Flow Labels**
   - Current: Generic login form with MRRG branding
   - Decision: Update Welcome text and error messages?
   - Recommendation: Use template variables for app name in messages

---

## Docker / Configuration Analysis

### Keep as Generic Platform Foundation

#### Docker Compose Development (`docker-compose.yml`)

**Services to Keep:**
- **backend** service
  - Maven build stage with Spring Boot application
  - Standard Spring Boot containerization
  - Environment variable configuration
  - KEEP AS-IS

- **frontend** service
  - Node.js and npm build
  - Vite build output served via nginx
  - KEEP AS-IS

- **postgres** service
  - PostgreSQL 16 Alpine image
  - Standard database setup with volumes
  - KEEP AS-IS

- **mailpit** service
  - Development email capture (replaces real SMTP)
  - KEEP AS-IS (excellent for dev)

**Configuration:**
- Network isolation (mrrg-network bridge)
- Service dependencies
- Volume management for database persistence
- Port mapping for development access
- Generic and reusable
- ACTION: Rename network from "mrrg-network" to "app-network"

#### Docker Compose Production (`docker-compose.prod.yml`)

**Overall Structure:**
- Multi-stage build setup
- Environment variable externalization
- Production-grade security considerations
- Database persistence
- Network isolation
- Excellent template for production
- KEEP STRUCTURE AS-IS, UPDATE LABELS

**Backend Service:**
- Maven multi-stage build
- Environment-based configuration
- Read-only Firebase service account mount
- Service health checks
- Stateless design
- KEEP AS-IS

**Frontend Service:**
- Node.js build with VITE_API_BASE_URL parameter
- Static file serving
- KEEP AS-IS

**PostgreSQL Service:**
- No public port exposure (commented out) — GOOD security practice
- Health checks
- Persistent volumes
- Environment variable configuration
- KEEP AS-IS

**PostgreSQL Configuration:**
- POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD from environment
- All externalized
- KEEP AS-IS

#### Environment Variable Strategy (KEEP)
- All secrets and configuration externalized
- No hardcoded production values
- Clear separation of dev/prod
- Uses .env file pattern (not in Git)
- EXCELLENT template for security
- KEEP STRUCTURE AS-IS

**Environment Variables to Keep:**
- SPRING_DATASOURCE_* (database connection)
- JWT_SECRET (authentication)
- FIREBASE_SERVICE_ACCOUNT_PATH (notifications)
- SPRING_MAIL_* (email)
- FRONTEND_ORIGIN (CORS)
- ACTIVATION_LINK_BASE_URL (account activation)
- SPRING_PROFILES_ACTIVE (environment selection)
- All are generic and reusable

#### Dockerfiles

**Backend Dockerfile:**
```
FROM maven:3.9.9-eclipse-temurin-21 AS build
...
FROM eclipse-temurin:21-jre-jammy
...
```
- Multi-stage build (Maven build → JRE runtime)
- Clean separation of build and runtime
- Minimal runtime image
- Port 4000 exposure
- Generic and excellent pattern
- KEEP AS-IS

**Frontend Dockerfile:**
```
FROM node:20-alpine
...
npm run build
...
serve -s dist -l 80
```
- Node.js Alpine (minimal image)
- Production build
- Static file serving
- Port 80 exposure
- Generic pattern
- KEEP AS-IS

#### Configuration Files

**application.properties:**
- Database configuration (KEEP)
- Mail configuration (KEEP)
- Firebase configuration (KEEP)
- Account activation configuration (KEEP)
- CORS configuration (KEEP)
- JWT configuration (KEEP)
- All generic, all externalized via environment variables
- KEEP AS-IS

**application-prod.properties:**
- Production-specific Spring profile
- Generic configuration
- KEEP AS-IS

### Generalize (Rename/Update)

1. **Network Name**
   - Change `mrrg-network` → `app-network` in docker-compose files
   - Change `mrrg-postgres-data` → `app-postgres-data`

2. **Service Names** (Optional)
   - Keep service names generic (backend, frontend, postgres, mailpit)
   - Services are already generic
   - NO CHANGE NEEDED

3. **Database Configuration**
   - Change default POSTGRES_DB: `mrrg` → `app` or `appdb`
   - Change default POSTGRES_USER: `mrrg` → `appuser`
   - Update .env.example accordingly

4. **Documentation in docker-compose.prod.yml**
   - Update comments referencing MRRG to generic template language
   - Keep all technical content

### Remove from Template

**Nothing to explicitly remove** — Docker configuration is entirely generic.

The only "MRRG-specific" aspects are naming conventions that should be generalized (network names, database names, etc.).

### Placeholder/Example

1. **.env.example file** — Create comprehensive template
   ```
   # Database
   SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/appdb
   SPRING_DATASOURCE_USERNAME=appuser
   SPRING_DATASOURCE_PASSWORD=changeme
   
   # Authentication
   JWT_SECRET=your-super-secret-key-256-chars-min
   
   # Firebase (optional)
   FIREBASE_SERVICE_ACCOUNT_PATH=/app/firebase-service-account.json
   
   # Email (SMTP)
   SPRING_MAIL_HOST=smtp.gmail.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=your-email@gmail.com
   SPRING_MAIL_PASSWORD=your-app-password
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
   SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
   SPRING_MAIL_FROM=noreply@yourdomain.com
   
   # Frontend
   VITE_API_BASE_URL=https://api.yourdomain.com
   FRONTEND_ORIGIN=https://yourdomain.com
   
   # Account Activation
   ACTIVATION_LINK_BASE_URL=https://yourdomain.com/activate
   ACTIVATION_TOKEN_EXPIRATION_HOURS=24
   
   # Spring Profile
   SPRING_PROFILES_ACTIVE=prod
   ```

2. **Reverse Proxy Example** — Add nginx configuration example
   - Show HTTPS/TLS setup
   - Show API routing patterns
   - Document security best practices

### Manual Decisions Required

1. **Local Database Credentials**
   - Current defaults: mrrg/mrrg/mrrgpass
   - Decision: Keep generic app/appuser/apppass or keep current for familiarity?
   - Recommendation: Generalize to app/appuser/apppass

2. **Firebase Integration**
   - Current: firebase-service-account.json required but optional
   - Decision: Make optional or required for template?
   - Recommendation: Keep optional, document Firebase setup guide

3. **SMTP Configuration**
   - Current: mailpit for dev, production needs real SMTP
   - Decision: Include SMTP provider recommendations?
   - Recommendation: Provide .env.example with SendGrid/AWS SES templates

4. **Reverse Proxy**
   - Current: docker-compose.prod.yml doesn't include reverse proxy
   - Decision: Add nginx/Caddy example or external documentation?
   - Recommendation: Add optional nginx service to docker-compose.prod.yml

---

## Documentation Analysis

### Keep as Generic Platform Foundation

#### Project Philosophy (`docs/philosophy/Project-Philosophy.md`)
- **Content:** Design principles emphasizing simplicity, business-driven development, maintainability
- **Applicability:** 100% generic and excellent template philosophy
- **Decision:** KEEP AS-IS — This is exactly the philosophy a template should embody
- **Action:** No changes needed; serves as model for business platform approach

#### Software Architecture (`docs/architecture/Software-Architecture.md`)
- **Content:** Layered architecture (Controller → Service → Repository), backend owns business rules, REST API design
- **Applicability:** 100% generic and architectural pattern-based
- **Current Content:**
  - System diagram: Backend ← → Mobile + Web clients ← → PostgreSQL
  - Responsibilities matrix
  - Data flow patterns
  - Notification flow patterns
  - Architectural principles
- **Decision:** KEEP STRUCTURE, GENERALIZE TERMINOLOGY
- **Action:**
  - Replace "MRRG-Mobile" with "[YourApp]-Mobile" or "Mobile Client"
  - Replace job/roofing examples with generic "business entity" examples
  - Keep all architectural patterns and design principles

#### Installation Guide (`docs/development/Installation-Guide.md`)
- **Purpose:** Local development environment setup
- **Applicability:** Generic Docker Compose-based setup
- **Decision:** KEEP AS-IS, UPDATE NAMING
- **Action:**
  - Update service names and database names to generic references
  - Keep all technical instructions
  - Update repository clone URL to template repository
  - Keep port numbers as-is (4000, 3000, etc.)

#### Maintenance Guide (`docs/development/Maintenance-Guide.md`)
- **Content:** Guidance for maintaining and extending the project
- **Applicability:** Generic maintenance patterns
- **Decision:** KEEP AS-IS, GENERALIZE CONTENT
- **Action:**
  - Remove MRRG-specific examples
  - Replace with generic business entity examples
  - Keep database upgrade procedures
  - Keep dependency management guidance

#### FCM Setup Guide (`docs/development/FCM-SETUP-GUIDE.md`)
- **Content:** Firebase Cloud Messaging integration instructions
- **Applicability:** Generic notification infrastructure documentation
- **Decision:** KEEP AS-IS — Firebase setup is platform-independent
- **Action:**
  - Remove MRRG branding
  - Keep all technical setup instructions
  - Keep security best practices
  - Keep configuration examples

#### Security Documentation (`docs/architecture/Security.md`)
- **Content:** Authentication strategy, JWT approach, password security
- **Applicability:** Generic security best practices
- **Decision:** KEEP AS-IS — Covers platform-level security, not business logic
- **Action:**
  - Keep all technical security content
  - Keep authentication flow diagrams
  - Keep secret management guidance
  - Remove any MRRG-specific security examples

### Generalize (Update Terminology)

1. **All Documentation**
   - Replace "MRRG" with "[YourApp]" or use template variable `{APP_NAME}`
   - Replace "roofing" examples with "business" examples
   - Replace "Margaret River Re-Gutter" with generic business descriptions
   - Replace "field workers" with "users" or "team members"
   - Replace "managers" with "administrators" where appropriate

2. **Architecture Diagram**
   - Replace mobile/web client labels with generic "[Client Type]"
   - Keep database and backend generic (already is)

3. **Feature Lists**
   - Current: Job scheduling, photo uploads, field reporting, callbacks
   - Generic: Entity management, workflow tracking, notifications, role-based access
   - General enough that examples work for most business domains

### Remove from Template

1. **Administrator Manual** (`docs/user/Administrator-Manual.md`)
   - This is likely MRRG-specific business process documentation
   - ACTION: REMOVE entirely, replace with template version

2. **User Manual** (`docs/user/User-Manual.md`)
   - This is likely MRRG-Mobile field worker documentation
   - OUT OF SCOPE for web template (Android deep links, FCM setup for mobile)
   - ACTION: REMOVE entirely

3. **MRRG-Specific Business Process Documentation**
   - Any documentation about job workflows, roofing processes, callbacks
   - ACTION: Remove all business-specific content

### Placeholder/Example

1. **Administrator Manual - Template Version** (`docs/user/Template-Administrator-Guide.md`)
   - Authentication and user management
   - Role-based access control
   - Notification system
   - System configuration
   - Backup and recovery procedures

2. **Developer Guide Extension** (`docs/development/Developer-Guide.md`)
   - How to add custom business entities
   - How to extend authentication/authorization
   - How to add new notification types
   - How to customize email templates
   - How to add new API endpoints

3. **Deployment Guide** (`docs/deployment/Production-Deployment-Guide.md`)
   - Reverse proxy setup (nginx/Caddy)
   - SSL/TLS configuration
   - Database backup strategy
   - Monitoring and logging
   - Scaling considerations

### Manual Decisions Required

1. **Documentation Language**
   - Use {APP_NAME} template variables or hardcode "Your App"?
   - Recommendation: Use clear comments like "Replace MRRG with your app name"

2. **Example Business Entities**
   - Keep generic or provide multiple examples (projects, tasks, orders)?
   - Recommendation: Keep generic, provide one example in separate documentation

3. **API Documentation**
   - Current: Swagger UI (auto-generated from code)
   - Decision: Keep as-is or create manual API guide?
   - Recommendation: Keep Swagger as-is, it will be auto-generated for template

---

## Proposed Extraction Phases

Break the extraction work into small, safe, commit-sized changes:

### Phase 1: Remove MRRG Branding and Terminology (1-2 commits)
**Objective:** Replace company/application name references throughout codebase

**Backend Changes:**
- [ ] Update EmailService email templates: Replace "MRRG" → "[APP_NAME]"
- [ ] Update application.properties: Update comments and examples
- [ ] Update docker-compose files: Rename networks/volumes from mrrg-* → app-*
- [ ] Update Dockerfile comments if any reference MRRG

**Frontend Changes:**
- [ ] Update Login.jsx: Replace MRRG logo with placeholder
- [ ] Update page titles and header text: Remove "MRRG" references
- [ ] Update CSS variables: Use generic color names

**Documentation Changes:**
- [ ] Update README.md: Replace company information
- [ ] Update architecture docs: Replace MRRG mobile references with generic [YourApp] Mobile
- [ ] Update philosophy docs: Keep as-is (already generic)

**Commit Message:** `refactor: remove MRRG branding and company-specific terminology`

---

### Phase 2: Generalize Notification Types (1 commit)
**Objective:** Rename job-specific notification types to generic event types

**Backend Changes:**
- [ ] Rename NotificationType enum values:
  - JOB_ASSIGNED → ENTITY_ASSIGNED
  - JOB_RESCHEDULED → ENTITY_SCHEDULED
  - JOB_READY_FOR_CONFIRMATION → ENTITY_READY_FOR_CONFIRMATION
  - JOB_CONFIRMED → ENTITY_CONFIRMED
- [ ] Update NotificationService.generateTitle() to use generic titles
- [ ] Update tests referencing these types

**Frontend Changes:**
- [ ] Update notification type references in NotificationPage
- [ ] Update notification display labels

**Database Consideration:**
- Notification types are stored in database
- Migration: Provide SQL migration script to update existing data
- Or: Provide forward compatibility with mapping

**Commit Message:** `refactor: generalize notification types from job-specific to entity-agnostic`

---

### Phase 3: Remove Job Domain Entirely (2-3 commits)

**Commit 3a: Remove Job Entities and Core Classes**
- [ ] Delete Job.java
- [ ] Delete JobStatus.java
- [ ] Delete JobType.java
- [ ] Delete StringListJsonConverter.java
- [ ] Delete JobRepository.java
- [ ] Update pom.xml if any Job-specific dependencies exist

**Commit 3b: Remove Job Services**
- [ ] Delete JobService.java
- [ ] Delete JobController.java
- [ ] Delete all job-related DTOs:
  - AssignWorkersRequest.java
  - CallbackFixRequest.java

**Commit 3c: Remove Job-Related Tests**
- [ ] Delete JobServiceTest.java
- [ ] Delete any integration tests referencing jobs

**Frontend Commits (2 commits):**

**Commit 3d: Remove Job Components**
- [ ] Delete JobList.jsx
- [ ] Delete JobCard.jsx
- [ ] Delete JobModal.jsx
- [ ] Delete WeekView.jsx
- [ ] Delete jobApi.js

**Commit 3e: Remove Job Pages and Styling**
- [ ] Delete MainDashboard.jsx
- [ ] Delete jobConfig.js constants
- [ ] Delete job-related CSS files:
  - JobList.css
  - JobModal.css
  - WeekView.css
  - Dashboard.css
- [ ] Delete job-related imports from App.jsx

**Commit Messages:**
- `refactor: remove Job entity and domain logic`
- `refactor: remove job services and controllers`
- `refactor: remove job-related tests`
- `refactor(frontend): remove job-specific components`
- `refactor(frontend): remove main dashboard and job styling`

---

### Phase 4: Generalize Backend Platform Foundation (1-2 commits)

**Backend Generalization:**
- [ ] Rename CreateEmployeeRequest.java → CreateUserRequest.java
- [ ] Review and update all service documentation comments
- [ ] Review and update all DTO documentation
- [ ] Update ApplicationProperties documentation
- [ ] Ensure all error messages are generic (no "job" references)

**Configuration:**
- [ ] Create comprehensive .env.example with all variables documented
- [ ] Update docker-compose.yml with generic service names
- [ ] Update docker-compose.prod.yml comments

**Commit Message:** `refactor: generalize backend user management and platform services`

---

### Phase 5: Generalize Frontend Admin Shell (1-2 commits)

**Frontend Generalization:**
- [ ] Rename AdminPage.jsx → AdministrationPage.jsx or keep AdminPage
- [ ] Update AdminPage to show generic entity management (not jobs)
- [ ] Remove all job-specific admin operations
- [ ] Create generic entity placeholder (ProjectCard, ProjectList as examples)
- [ ] Update navigation labels to be generic
- [ ] Standardize all button labels and messages

**Commit Message:** `refactor(frontend): generalize admin dashboard and navigation`

---

### Phase 6: Add Template Examples (1-2 commits)

**Backend:**
- [ ] Create `src/main/java/com/template/backend/example/` package
- [ ] Add ExampleProject.java (template entity showing patterns)
- [ ] Add ExampleProjectService.java (template service showing patterns)
- [ ] Add ExampleProjectController.java (template controller showing patterns)
- [ ] Document how to use as starting point

**Frontend:**
- [ ] Create `src/components/examples/` directory
- [ ] Add ProjectCard.jsx (example component)
- [ ] Add ProjectService.jsx (example service wrapper)
- [ ] Add ProjectManagementExample.jsx (example page)
- [ ] Document patterns for extension

**Commit Message:** `docs: add template example implementations for custom entities`

---

### Phase 7: Update and Create Template Documentation (1-2 commits)

**Documentation Updates:**
- [ ] Replace README.md with template README (overview, getting started, architecture, examples)
- [ ] Update Installation-Guide.md: Generic setup instructions
- [ ] Update Maintenance-Guide.md: Generic maintenance procedures
- [ ] Update Security.md: Keep as-is
- [ ] Remove Administrator-Manual.md
- [ ] Remove User-Manual.md
- [ ] Create Developer-Guide.md: How to extend the template
- [ ] Create EXTRACTION-NOTES.md: What was removed and why

**Architecture Documentation:**
- [ ] Update Software-Architecture.md: Generic client/backend description
- [ ] Keep Project-Philosophy.md: Excellent for template

**Commit Message:** `docs: update and create template documentation`

---

### Phase 8: Final Validation and README (1 commit)

**Validation Tasks:**
- [ ] Backend builds: `mvn clean package`
- [ ] Frontend builds: `npm run build`
- [ ] Docker Compose dev: `docker compose up` starts all services
- [ ] Backend API accessible on port 4000
- [ ] Frontend accessible on port 3000
- [ ] Login flow works (no job-specific logic interferes)
- [ ] User management works
- [ ] Notifications work
- [ ] No compilation errors or warnings from removed code

**Final Documentation:**
- [ ] Create BUSINESS_PLATFORM_TEMPLATE.md (this becomes the template guide)
- [ ] Create TEMPLATE_CUSTOMIZATION.md (guide for using template for new business)
- [ ] Update README.md with template usage examples

**Commit Message:** `chore: final template validation and documentation`

---

## Risks and Mitigation

### Critical Risks

#### 1. Accidentally Deleting Reusable Notification Logic ⚠️ HIGH RISK
**Risk:** Remove job-specific notification logic but accidentally delete generic notification service
**Mitigation:**
- Notification system is clearly separated: NotificationService (generic) vs. JobService notification calls (specific)
- Keep all generic Notification entity, Repository, Controller, Service
- Only remove job-specific notification type names (phase 2)
- **Test Plan:** All NotificationService tests must pass after extraction

#### 2. Breaking Authentication and JWT ⚠️ CRITICAL
**Risk:** Authentication system intertwined with job business logic somehow
**Mitigation:**
- Audit shows: Auth, JWT, SecurityConfig have ZERO dependencies on Job
- Authentication is completely isolated
- Safe to remove all job code without touching auth
- **Test Plan:** All AuthService and AuthController tests must pass

#### 3. Breaking Account Activation Flow ⚠️ CRITICAL
**Risk:** Activation system depends on job-related configuration
**Mitigation:**
- Account activation completely independent of jobs
- Token system, Email service, User management work standalone
- No configuration dependencies on job domain
- **Test Plan:** All ActivationService tests must pass, manual account activation flow

#### 4. Breaking User Lifecycle Management ⚠️ CRITICAL
**Risk:** User creation, deactivation, reactivation logic depends on jobs
**Mitigation:**
- Audit shows: UserManagementService is completely generic
- No job dependencies in user lifecycle
- **Test Plan:** All UserManagementService tests must pass, manual admin workflows

#### 5. Leaving MRRG-Specific Terminology Hidden ⚠️ MEDIUM RISK
**Risk:** Missed MRRG references in comments, error messages, or constants
**Mitigation:**
- Search repository for "MRRG", "mrrg", "roofing", "gutter", "margaret", "callback"
- Review all string literals and comments
- **Test Plan:** Grep/regex search for missed references before publishing

#### 6. Creating Over-Abstraction When Generalizing ⚠️ MEDIUM RISK
**Risk:** Introduce unnecessary generic abstractions that make code harder to understand
**Mitigation:**
- Follow project philosophy: "Simplicity before abstraction"
- Rename only what's necessary (NotificationType enum, branding)
- Don't create abstract base classes or interfaces for single implementations
- Keep examples as concrete implementations, not abstract patterns
- **Test Plan:** Code review focusing on simplicity

#### 7. Breaking Docker Development Setup ⚠️ MEDIUM RISK
**Risk:** Changes to docker-compose.yml break local dev environment
**Mitigation:**
- Only rename networks and volumes (from mrrg-* to app-*)
- No service structure changes
- Keep all port mappings identical
- Keep mailpit for development email
- **Test Plan:** `docker compose up --build` succeeds, services start correctly

#### 8. Breaking Frontend Routes and Redirects ⚠️ MEDIUM RISK
**Risk:** Removing MainDashboard breaks routing, navigation, or authentication flow
**Mitigation:**
- Routes: /login, /admin, /notifications, /users remain (only /jobs removed)
- PrivateRoute component untouched
- AuthContext untouched
- Replace MainDashboard with generic admin dashboard on / route
- **Test Plan:** Navigation, login redirect, private routes all work

#### 9. Deleting Useful Test Infrastructure ⚠️ MEDIUM RISK
**Risk:** Remove job tests that actually demonstrate testing patterns
**Mitigation:**
- Audit shows: Job tests are business-logic focused, not infrastructure patterns
- Keep test structure, replace content with generic user/auth tests
- Preserve test fixtures and mocking patterns
- **Test Plan:** Test suite runs, coverage maintained

#### 10. Circular Dependencies in Removal ⚠️ MEDIUM RISK
**Risk:** Job service removed but still referenced by notification or other code
**Mitigation:**
- Audit shows: Only JobService, JobController, DTOs reference Job entity
- No circular dependencies
- Clean removal possible
- **Test Plan:** Compilation succeeds after each phase, no missing dependencies

---

## Final Recommendation

### Overall Assessment: ✅ HIGHLY RECOMMENDED FOR TEMPLATE EXTRACTION

**MRRG is an excellent foundation for a Business Platform Template because:**

1. **Clean Architecture** — Strict separation between generic platform infrastructure (auth, users, notifications, email) and business domain (jobs, scheduling, photos, callbacks)

2. **Production Readiness** — Comprehensive Docker setup, environment-based configuration, security best practices, JWT authentication

3. **Proven Design** — Architecture and code patterns have been validated in production use

4. **Explicit Simplicity** — Code avoids unnecessary abstractions, making it easy to understand and extend

5. **Complete Feature Set** — Includes every platform service needed for a business app (auth, user management, notifications, email, Firebase integration)

6. **Test Coverage** — Core services have unit tests demonstrating testing patterns

7. **Well-Documented** — Philosophy, architecture, and deployment decisions are clearly explained

8. **Minimal Extraction Effort** — Business domain is so cleanly separated that removal is straightforward

### Extraction Difficulty: ⭐⭐☆☆☆ (2/5 - Easy)
- **Low Complexity:** Clear separation of concerns makes extraction straightforward
- **Low Risk:** Generic platform infrastructure has zero dependencies on business logic
- **Predictable:** Following 7-8 phased commits leads to a clean, extraction-free template
- **Well-Defined:** Each phase has clear objectives and validation criteria

### Recommended Next Steps

#### Before Starting Extraction:
1. **Create extraction branch:** `git checkout -b refactor/template-extraction`
2. **Run full test suite:** Ensure current state passes all tests
3. **Document baseline:** Record current test coverage and performance metrics

#### Extraction Approach:
1. **Execute phases sequentially**, one per commit
2. **Test after each phase:** Run `mvn clean package` and `npm run build`
3. **Manual validation:** 
   - Start `docker compose up`
   - Test login flow
   - Test user management
   - Test notifications
   - Verify no errors in console
4. **Review before merge:** Each phase should be reviewed for removed MRRG references

#### Post-Extraction (Template Preparation):
1. **Rename package:** Consider renaming from `com.mrrg.backend` to `com.businessplatform.backend` or `com.template.backend`
2. **Create template README:** Guide for first-time template users
3. **Add customization guide:** Document where to add business-specific code
4. **Create example entity:** Show how to create first business entity
5. **Set up CI/CD:** GitHub Actions for template validation
6. **Version as 1.0.0**: Mark completion as v1.0.0 template release

### Warning: Scope Creep to Avoid

**Do NOT introduce during extraction:**
- Clean Architecture patterns
- Hexagonal Architecture
- CQRS
- DDD tactical patterns
- Microservices
- Over-generalized abstractions

**Template should remain:**
- Simple, explicit, straightforward
- Easy for new projects to understand and extend
- Free of architectural ceremony
- Focused on solving real business problems

---

## Conclusion

The MRRG web project is an **excellent, production-tested foundation** for a Business Platform Template. Its clean layered architecture, comprehensive platform services, and explicit business domain separation make extraction straightforward and low-risk.

Following the proposed 7-8 phase extraction plan will yield a clean, maintainable, production-ready template that teams can use to quickly bootstrap new business applications while maintaining MRRG's proven philosophy of simplicity, maintainability, and business-focused development.

The template will be approximately 30-40% smaller than MRRG (removing job domain), retain 100% of platform infrastructure, and provide clear examples and patterns for adding business-specific functionality.

**Recommendation: Proceed with extraction. Begin with Phase 1 (branding removal) to gain confidence, then continue through Phase 8 (final validation).**
