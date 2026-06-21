# Software Architecture

## Overview

The Business Platform Template is composed of a single backend and one or more client applications.

The backend owns the business rules and is responsible for data consistency, authentication, authorization and notification management.

Each client focuses on a specific audience while relying on the same REST API.

```text
                         ┌────────────────────────────┐
                         │        PostgreSQL          │
                         │      Persistent Data       │
                         └────────────▲───────────────┘
                                      │
                                      ▼
                         ┌────────────────────────────┐
                         │      Spring Boot API       │
                         │ Business Rules / Security  │
                         └─────────────┬──────────────┘
                                       │
                                REST API│
                                       │
                    ┌──────────────────▼──────────────────┐
                    │   React Web Client                 │
                    │   Managers/Admins                  │
                    └──────────────────────────────────────┘
```

---

## System Components

### Spring Boot Backend

The backend is responsible for every infrastructure and business decision within the system.

It validates requests, enforces permissions, manages business workflows, coordinates data persistence and creates notifications.

Client applications never contain business logic that could lead to inconsistent behaviour.

---

### React Web Application

The React application is used by administrators and managers.

It provides an admin shell and tools for managing platform users.

---

### PostgreSQL

PostgreSQL is the system of record for all persistent application data.

All clients access the same information through the backend, ensuring a single and consistent source of data.

---

### Firebase Cloud Messaging

Firebase Cloud Messaging is used exclusively to deliver push notifications.

Notifications are first persisted by the backend before being sent to client devices.

If delivery fails, the notification remains available inside the application.

---

## System Responsibilities

| Component | Responsibility |
|-----------|----------------|
| Backend | Business rules, authentication, persistence, notifications |
| React | Administration and management interface |
| PostgreSQL | Persistent storage |
| Firebase | Push notification delivery |

---

## Business Data Flow

All business operations follow the same principle.

```text
User Action

   ↓

REST API

   ↓

Business Rules

   ↓

Database

   ↓

Response

   ↓

Client Update
```

Because every request passes through the backend, all clients always follow the same business rules.

---

## Notification Flow

Notifications originate from business events.

```text
Business Event

↓

Backend

↓

Save Notification

↓

Send FCM (optional)

↓

Client Device
```

The backend remains the owner of the notification lifecycle.

Firebase only delivers notifications and does not store application data.

---

## Architectural Principles

The architecture is guided by a small number of principles.

- A single backend owns every business rule.
- All clients share the same REST API.
- PostgreSQL is the only persistent data store.
- Push notifications are persisted before delivery.
- Business behaviour remains identical regardless of the client being used.
- Client applications never become sources of business truth.
