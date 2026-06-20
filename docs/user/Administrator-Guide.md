# Administrator Guide

## Introduction

This guide describes the administrative workflows available in MRRG.

It is intended for managers and administrators responsible for scheduling work, managing employees and monitoring job progress.

The screenshots shown throughout this document illustrate the current version of the application. Minor interface changes may occur over time while the underlying workflows remain unchanged.

---

# User Management

Administrators are responsible for creating and maintaining user accounts.

Employees cannot register themselves.

Each new account is created by an administrator and remains in the `PENDING_ACTIVATION` state until the employee completes the account activation process from the Android application.

Administrators can:

* create new users;
* update user information;
* deactivate existing accounts;
* reactivate disabled accounts;
* resend activation emails.

> *(Insert User Management screenshot)*

---

# Scheduling Work

Jobs are created and scheduled through the management interface.

Scheduling consists of assigning the appropriate employee, selecting the planned work date and defining the expected working period.

Once scheduled, the assigned employee automatically receives the job through the Android application.

Managers may reschedule jobs whenever operational requirements change.

> *(Insert Scheduling screenshot)*

---

# Managing Jobs

Jobs move through a predefined business workflow.

```text
Pending

↓

Scheduled

↓

In Progress

↓

Waiting Manager Validation

↓

Completed

↓

Archived
```

Each status represents a business milestone and ensures that work progresses in a controlled and predictable manner.

Managers monitor this progression from the dashboard and intervene only when business decisions are required.

---

# Validating Completed Work

After completing a job, field workers upload the required photographs and submit the work for validation.

Managers review the submitted information before marking the job as completed.

Validation confirms that the work satisfies business requirements before it becomes part of the company's archive.

> *(Insert Validation screenshots)*

---

# Archive and Callbacks

Completed jobs are moved to the archive.

If a customer requests additional work or reports an issue, archived jobs can be restored to the active scheduling queue.

This preserves the complete history of the original work while allowing managers to continue the workflow without creating duplicate records.

> *(Insert Archive / Callback screenshots)*

---

# Notifications

MRRG provides notifications for important business events.

Typical notifications include:

* newly assigned work;
* schedule changes;
* completed jobs awaiting validation.

Notifications help managers and employees remain informed without requiring constant manual communication.

> *(Insert Notification screenshots)*

---

# Good Practices

To maintain accurate operational records:

* schedule jobs before assigning field workers;
* validate completed work promptly;
* archive completed jobs only after verification;
* deactivate accounts instead of deleting users;
* restore archived jobs for callbacks instead of creating new jobs.

Following these practices preserves the integrity of business history while ensuring consistent workflows across the application.
