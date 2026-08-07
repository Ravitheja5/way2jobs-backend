package com.way2jobs.controller;

import com.way2jobs.dto.AdminDashboardResponse;
import com.way2jobs.dto.AdminLoginRequest;
import com.way2jobs.dto.AdminLoginResponse;
import com.way2jobs.dto.BulkJobRequest;
import com.way2jobs.dto.JobRequest;
import com.way2jobs.entity.Job;
import com.way2jobs.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ================= AUTHENTICATION =================

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @RequestBody @Valid AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    // ================= DASHBOARD =================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ================= GET ALL JOBS =================

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(adminService.getAllJobs());
    }

    // ================= ADD JOB =================

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(
            @RequestBody @Valid JobRequest request) {

        Job job = adminService.createJob(request);

        return new ResponseEntity<>(job, HttpStatus.CREATED);
    }

    // ================= UPDATE JOB =================

    @PutMapping("/jobs/{id}")
    public ResponseEntity<Job> updateJob(
            @PathVariable Long id,
            @RequestBody @Valid JobRequest request) {

        return ResponseEntity.ok(adminService.updateJob(id, request));
    }

    // ================= DELETE JOB =================

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {

        adminService.deleteJob(id);

        return ResponseEntity.ok("Job deleted successfully.");
    }

    // ================= BULK IMPORT =================

    @PostMapping("/jobs/bulk-import")
    public ResponseEntity<java.util.Map<String, String>> bulkImport(
            @RequestBody BulkJobRequest request) {
        String result = adminService.bulkImport(request);
        return ResponseEntity.ok(java.util.Map.of("message", result));
    }

}
