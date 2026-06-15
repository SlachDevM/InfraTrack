## MRRG
Demo Web application for my roofing company (Margaret River Re-Gutter) that aim to help the managment part to schedule jobs and manage employees assignment. Also aim to help employees to have a better overview of the jobs and upload photos of the jobs. Global picture, is to fluidify the workflow

#Stack
Frontend : ReactJs (using the colors and logo of the Margaret River Re-Gutter website)
backend : Java 21 using framework Spring
database : Postgres

#workflow
The user log in through a login page on which he can register or login.
Once logged in, the user land on the main page of the app which show the current week and a list of the pending jobs.
Employees can then see their jobs for the week. Once they arrive on site, they take photos of the job (before photos) and upload it to the assigned job. Once the job is completed, they take photos (after photo) and eventually add notes before clicking the "completed job" button.
The manager can then validate the job that will go to the archived section on the manager page that only them can access.
If a client call back for a fix, the manager can "re open" the job that will go on "To Be Fixed" status which will bring back the job to the main page with the highest rank of priority so that this job can easily be scheduled asap.

#Roles
There are 3 different roles : Admin, Manager and Employee.
The admin role is now to be defined
The Manager role allow to create new jobs, update jobs, schedule jobs and assign workers to jobs. It also allow to validate jobs once they are marked as completed by the assigned workers.
The Employee role is the lowest rank and only allow to access the main page and see the schedule of the current week (and the list of pending jobs so that everyone on the company can see what jobs are left to do). 
Every employee can upload photos and add comments to their assigned jobs.

#Notifications
Managers receive notifications once a job is marked as completed by the assigned workers
Employees receive notifications once a job is assigned to them or when an assigned job is rescheduled
