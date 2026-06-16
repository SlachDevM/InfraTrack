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
- Download photos  
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
Frontend : 
- ReactJs (using the colors and logo of the Margaret River Re-Gutter website)  

backend :  
- Java 21  
- Spring Boot  
- Spring Security  
- Spring Data JPA  
- REST API architecture
   
database : 
- Postgres 

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
The manager can then validate the job that will go to the done section on the manager page that only them can access.  
If a client calls back for a fix, the manager can "re-open" the job that will go on "To Be Fixed" status which brings the job back to the main page with the highest rank of priority so that this job can easily be scheduled as soon as possible.
The manager can archive the job once it's concidered as fully finished with no chance of callback. (there is still a callback option to set the job back in the workflow once archived).

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

# Screenshot

Login page :  
<img width="964" height="1256" alt="image" src="https://github.com/user-attachments/assets/f92cc585-5ecc-45de-891a-3712e4793f1d" />

Main page :  
<img width="2812" height="1508" alt="image" src="https://github.com/user-attachments/assets/fc0c4112-7c68-4e2f-a8a6-fcdba8dd9fb0" />

Job creation window :  
<img width="1244" height="1498" alt="image" src="https://github.com/user-attachments/assets/3ffd18d2-9cee-4aa4-8eff-304891b4b015" />
<img width="1252" height="748" alt="image" src="https://github.com/user-attachments/assets/597f8859-3009-486e-ba8a-f8314f3d5fda" />

Pool of jobs to be assigned :
<img width="2786" height="914" alt="image" src="https://github.com/user-attachments/assets/76aea401-1154-41c7-9039-da9eff38b95e" />

Edit job window with photo managment :
<img width="1254" height="1512" alt="image" src="https://github.com/user-attachments/assets/5e168306-3399-44d0-91d7-2e204a1d5795" />

Main page with notifications up :
<img width="2802" height="1518" alt="image" src="https://github.com/user-attachments/assets/2cd77d84-d5ea-46ba-bffc-45cf38de9652" />

Notification page :  
<img width="2834" height="1520" alt="image" src="https://github.com/user-attachments/assets/2e0cde92-b1bd-48ba-9bcc-469d3b2dad62" />

Job window for assigned worker on a job with completed job option :
<img width="1242" height="1496" alt="image" src="https://github.com/user-attachments/assets/bdabb9ee-f46b-4ee6-9d16-2b3dd8708a37" />

Job waiting for manager validation once marked as done by worker :
<img width="480" height="526" alt="image" src="https://github.com/user-attachments/assets/ef45badd-fec5-443f-ae7c-cbec30dd3cf1" />

Manager notification for validation : 
<img width="2852" height="858" alt="image" src="https://github.com/user-attachments/assets/d6e601fd-8cfb-4fde-a07d-12372702b3d7" />

Job window for manager (once waiting for validation) :
<img width="1238" height="1504" alt="image" src="https://github.com/user-attachments/assets/47bfbb5d-ffea-401b-a0e6-08fa03a28f95" />

Admin page :  
<img width="2820" height="1440" alt="image" src="https://github.com/user-attachments/assets/38dcd062-3b27-42b7-a55a-cb248e71785f" />

callback or archive option for done jobs :
<img width="1250" height="1494" alt="image" src="https://github.com/user-attachments/assets/5a6c07cb-66c9-4b38-a5d6-6b7425c775b0" />

Job back to pool waiting to be fixed after callback : 
<img width="2702" height="874" alt="image" src="https://github.com/user-attachments/assets/1625b3a1-5683-46ad-871a-a9aa1ac5eec7" />

Archived job :
<img width="2822" height="1454" alt="image" src="https://github.com/user-attachments/assets/cc7d770a-cc77-4904-8007-b5ec89e1c0db" />
