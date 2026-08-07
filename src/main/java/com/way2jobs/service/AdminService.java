package com.way2jobs.service;

import com.way2jobs.dto.AdminDashboardResponse;
import com.way2jobs.dto.AdminLoginRequest;
import com.way2jobs.dto.AdminLoginResponse;
import com.way2jobs.dto.BulkJobRequest;
import com.way2jobs.dto.JobRequest;
import com.way2jobs.entity.Job;

import java.util.List;

public interface AdminService {

    // Authentication
    AdminLoginResponse login(AdminLoginRequest request);

    // Dashboard
    AdminDashboardResponse getDashboard();

    // Job CRUD
    List<Job> getAllJobs();

    Job createJob(JobRequest request);

    Job updateJob(Long id, JobRequest request);

    void deleteJob(Long id);

    // Bulk Import
    String bulkImport(BulkJobRequest request);
}