# Installation Guide

## Introduction

This guide describes the steps required to deploy a local development instance of the MRRG ecosystem.

The project consists of a Spring Boot backend, a React web application and a PostgreSQL database. Docker Compose is used to simplify local deployment.

---

## Prerequisites

Before starting, ensure the following software is available:

* Docker
* Docker Compose
* Git

A Firebase service account is also required to enable push notification support.

---

## Project Structure

Clone the backend repository.

```bash
git clone https://github.com/SlachDevM/MRRG.git

cd MRRG
```

Clone the Android application separately if mobile development is required.

```bash
git clone https://github.com/SlachDevM/MRRG-Mobile.git
```

---

## Firebase Configuration

Place the Firebase service account JSON file inside the backend directory.

```
backend/
└── firebase-service-account.json
```

The Docker Compose configuration mounts this file automatically when the backend container starts.

---

## Start the Environment

Launch the complete development environment.

```bash
docker compose up --build
```

The following services will be started:

| Service         | Default Address                             |
| --------------- | ------------------------------------------- |
| React Web       | http://localhost:3000                       |
| Spring Boot API | http://localhost:4000                       |
| Swagger UI      | http://localhost:4000/swagger-ui/index.html |
| PostgreSQL      | localhost:5432                              |

---

## Email Testing

For local development, account activation emails can be tested using Mailpit.

When enabled in the Docker Compose configuration, Mailpit provides:

| Service       | Default Address       |
| ------------- | --------------------- |
| SMTP          | localhost:1025        |
| Web Interface | http://localhost:8025 |

This allows account activation emails to be tested without an external SMTP provider.

---

## Android Development

The Android application communicates directly with the Spring Boot backend.

When using the Android emulator, ensure that the API base URL points to the host machine using:

```
http://10.0.2.2:4000
```

instead of `localhost`.

---

## Initial Setup

After the environment has started successfully:

1. Verify that the backend is accessible through Swagger UI.
2. Verify that the React application loads correctly.
3. Create an administrator account if the database is empty.
4. Create employee accounts from the administration interface.
5. Complete account activation from the Android application.

The system is now ready for development and testing.

---

## Troubleshooting

If the application does not start correctly, verify:

* Docker containers are running.
* PostgreSQL is accessible.
* The Firebase service account file is present.
* The configured API URL matches the running backend.
* Mailpit is running when testing email activation.

Most local setup issues are caused by missing configuration rather than application errors.
