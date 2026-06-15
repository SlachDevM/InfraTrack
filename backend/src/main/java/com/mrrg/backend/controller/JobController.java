package com.mrrg.backend.controller;

import com.mrrg.backend.model.*;
import com.mrrg.backend.repository.JobRepository;
import com.mrrg.backend.repository.NotificationRepository;
import com.mrrg.backend.repository.UserRepository;
import com.mrrg.backend.security.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:3000")
public class JobController {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Job>> listJobs(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Long userId = token.getUserId();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Job> jobs;
        if (user.getRole() == UserRole.EMPLOYEE) {
            jobs = jobRepository.findByStatusInOrderByPriorityLevelDesc(
                    Arrays.asList(JobStatus.PENDING, JobStatus.SCHEDULED, JobStatus.TO_BE_FIXED)
            );
        } else {
            jobs = jobRepository.findAll();
        }

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Job>> getPendingAndToBeFixed(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        List<Job> jobs = jobRepository.findByStatusInOrderByPriorityLevelDesc(
                Arrays.asList(JobStatus.PENDING, JobStatus.TO_BE_FIXED)
        );
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/done")
    public ResponseEntity<List<Job>> getDoneJobs(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Job> jobs = jobRepository.findByStatusOrderByPriorityLevelDesc(JobStatus.DONE);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<Job>> getArchivedJobs(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Job> jobs = jobRepository.findByStatusOrderByPriorityLevelDesc(JobStatus.ARCHIVED);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<Job>> getScheduledJobs(
            @RequestParam("weekStart") Long weekStart,
            @RequestParam("weekEnd") Long weekEnd,
            Authentication authentication) {
        List<Job> jobs = jobRepository.findByStatusAndJobDateBetweenOrderByJobStartHourAsc(
                JobStatus.SCHEDULED, weekStart, weekEnd);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable("id") Long id, Authentication authentication) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        job.setStatus(JobStatus.PENDING);
        job.setCreatedBy(token.getUserId());
        Job savedJob = jobRepository.save(job);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable("id") Long id, @RequestBody Job jobUpdate, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return jobRepository.findById(id)
                .map(job -> {
                    if (jobUpdate.getClientName() != null) {
                        job.setClientName(jobUpdate.getClientName());
                    }
                    if (jobUpdate.getClientPhone() != null) {
                        job.setClientPhone(jobUpdate.getClientPhone());
                    }
                    if (jobUpdate.getClientAddress() != null) {
                        job.setClientAddress(jobUpdate.getClientAddress());
                    }
                    if (jobUpdate.getDetails() != null) {
                        job.setDetails(jobUpdate.getDetails());
                    }
                    if (jobUpdate.getPriorityLevel() != null) {
                        job.setPriorityLevel(jobUpdate.getPriorityLevel());
                    }
                    if (jobUpdate.getNotes() != null) {
                        job.setNotes(jobUpdate.getNotes());
                    }
                    if (jobUpdate.getJobTypes() != null) {
                        job.setJobTypes(jobUpdate.getJobTypes());
                    }
                    if (jobUpdate.getStatus() != null) {
                        job.setStatus(jobUpdate.getStatus());
                    }
                    if (jobUpdate.getJobDate() != null) {
                        job.setJobDate(jobUpdate.getJobDate());
                        if (job.getStatus() == JobStatus.PENDING || job.getStatus() == JobStatus.TO_BE_FIXED) {
                            job.setStatus(JobStatus.SCHEDULED);
                        }
                    }
                    if (jobUpdate.getJobStartHour() != null) {
                        job.setJobStartHour(jobUpdate.getJobStartHour());
                    } else if (job.getJobDate() != null && job.getJobStartHour() == null) {
                        job.setJobStartHour("07:50");
                    }
                    if (jobUpdate.getAssignedWorkers() != null) {
                        job.setAssignedWorkers(jobUpdate.getAssignedWorkers());
                    }
                    if (jobUpdate.getBeforePhoto() != null) {
                        job.setBeforePhoto(jobUpdate.getBeforePhoto());
                    }
                    if (jobUpdate.getAfterPhoto() != null) {
                        job.setAfterPhoto(jobUpdate.getAfterPhoto());
                    }

                    job.setUpdatedAt(System.currentTimeMillis());
                    Job savedJob = jobRepository.save(job);

                    if (jobUpdate.getAssignedWorkers() != null && !jobUpdate.getAssignedWorkers().isEmpty()) {
                        String message = "You have been assigned to job: " + job.getClientName();
                        notificationRepository.save(new Notification(token.getUserId(), id, NotificationType.JOB_ASSIGNED, message));
                    }

                    return ResponseEntity.ok(savedJob);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign-workers")
    public ResponseEntity<?> assignWorkers(@PathVariable("id") Long id, @RequestBody Map map, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return jobRepository.findById(id)
                .map(job -> {
                    String workers = (String) map.get("assignedWorkers");
                    job.setAssignedWorkers(workers);
                    job.setUpdatedAt(System.currentTimeMillis());
                    Job savedJob = jobRepository.save(job);

                    String message = "You have been assigned to job: " + job.getClientName();
                    notificationRepository.save(new Notification(token.getUserId(), id, NotificationType.JOB_ASSIGNED, message));

                    return ResponseEntity.ok(savedJob);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeJob(@PathVariable("id") Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;

        return jobRepository.findById(id)
                .map(job -> {
                    job.setStatus(JobStatus.DONE);
                    job.setUpdatedAt(System.currentTimeMillis());
                    Job savedJob = jobRepository.save(job);

                    notificationRepository.save(new Notification(job.getCreatedBy(), id, NotificationType.JOB_READY_FOR_CONFIRMATION, "Job " + job.getClientName() + " is ready for confirmation"));

                    return ResponseEntity.ok(savedJob);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmJob(@PathVariable("id") Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return jobRepository.findById(id)
                .map(job -> {
                    job.setStatus(JobStatus.DONE);
                    job.setUpdatedAt(System.currentTimeMillis());
                    Job savedJob = jobRepository.save(job);
                    return ResponseEntity.ok(savedJob);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveJob(@PathVariable("id") Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return jobRepository.findById(id)
                .map(job -> {
                    job.setStatus(JobStatus.ARCHIVED);
                    job.setUpdatedAt(System.currentTimeMillis());
                    Job savedJob = jobRepository.save(job);
                    return ResponseEntity.ok(savedJob);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable("id") Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return jobRepository.findById(id)
                .map(job -> {
                    jobRepository.delete(job);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

class Map {
    private String assignedWorkers;

    public String getAssignedWorkers() {
        return assignedWorkers;
    }

    public void setAssignedWorkers(String assignedWorkers) {
        this.assignedWorkers = assignedWorkers;
    }

    public Object get(String key) {
        if ("assignedWorkers".equals(key)) {
            return assignedWorkers;
        }
        return null;
    }
}
