# MRRG

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql)

MRRG is a web application designed for the roofing company Margaret River Re-Gutter. 

The application helps managers schedule roofing jobs, assign employees, track job progress and manage completed work through a centralized workflow system. 

Employees can access their assigned jobs, upload before/after photos directly from the field and communicate job updates in real time. 

# Features

- Employee scheduling  
- Job assignment management  
- Task management linked to jobs  
- Before/after photo uploads  
- Job lifecycle and validation workflow  
- Archived jobs management  
- Re-open and priority escalation system  
- Real-time notifications  
- Role-based access control  
- Dynamic job status workflow  

# Technical Features

- JWT-based authentication and authorization  
- Spring Security integration  
- Protected REST API endpoints  
- JPA/Hibernate entity management  
- Repository pattern implementation  
- PostgreSQL relational database  
- Full Docker containerization  
- Dedicated frontend/backend containers  
- Isolated development environment  
- Service-oriented backend architecture  

# Stack
Frontend : ReactJs (using the colors and logo of the Margaret River Re-Gutter website)  
backend :  
- Java 21  
- Spring Boot  
- Spring Security  
- Spring Data JPA  
- REST API architecture  
database : Postgres 

The application is fully containerized using Docker, with a dedicated image for the frontend, backend and database services.

# Architecture

- Service-oriented architecture  
- RESTful API architecture  
- Dedicated controllers for jobs, tasks, users and notifications

# Authentication

Authentication is handled using JWT tokens with role-based authorization. 

Available roles: 
- Admin 
- Manager 
- Employee 

# Database

PostgreSQL is used to store: 
- users 
- jobs 
- schedules 
- notifications 
- uploaded media references

# Workflow
The user logs in through a login page where they can register or log in.
Once logged in, the user lands on the main page of the app which shows the current week and a list of the pending jobs.  
Employees can then see their jobs for the week. Once they arrive on site, they take photos of the job (before photos) and upload it to the assigned job. Once the job is completed, they take photos (after photo) and eventually add notes before clicking the "completed job" button.  
The manager can then validate the job that will go to the archived section on the manager page that only them can access.  
If a client calls back for a fix, the manager can "re-open" the job that will go on "To Be Fixed" status which brings the job back to the main page with the highest rank of priority so that this job can easily be scheduled as soon as possible.

# Roles
There are 3 different roles : Admin, Manager and Employee.  
The Admin role is currently under development.   
The Manager role allows managers to create new jobs, update jobs, schedule jobs and assign workers to jobs. It also allows managers to validate jobs once they are marked as completed by the assigned workers.  
The Employee role is the lowest rank and only allows access to the main page and allows employees to view the current week's schedule (and the list of pending jobs so that everyone on the company can see what jobs are left to do).  
Every employee can upload photos and add comments to their assigned jobs.

# Notifications
Managers receive notifications once a job is marked as completed by the assigned workers.  
Employees receive notifications once a job is assigned to them or when an assigned job is rescheduled 

# Installation

Documentation will be added soon.

# License

MIT

# API Documentation

Swagger/OpenAPI documentation coming soon.
