package com.way2jobs.service.impl;

import com.way2jobs.dto.*;
import com.way2jobs.entity.Category;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.State;
import com.way2jobs.service.CategoryService;
import com.way2jobs.service.DepartmentService;
import com.way2jobs.service.JobService;
import com.way2jobs.service.StateService;
import com.way2jobs.service.AdminService;
import com.way2jobs.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

        private final JobService jobService;
        private final DepartmentService departmentService;
        private final CategoryService categoryService;
        private final JwtUtil jwtUtil;
        private final StateService stateService;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {

        if ("admin".equals(request.getUsername())
                && "admin123".equals(request.getPassword())) {

            return new AdminLoginResponse(
                    true,
                    "Login Successful",
                    jwtUtil.generateToken(request.getUsername(), "ADMIN"),
                    request.getUsername()
            );
        }

        return new AdminLoginResponse(
                false,
                "Invalid Username or Password",
                null,
                null
        );
    }

    @Override
    public AdminDashboardResponse getDashboard() {

        long totalJobs = jobService.getAllJobs().size();
        long totalDepartments = departmentService.getAllDepartments().size();
        long totalCategories = categoryService.getAllCategories().size();
        long totalStates = stateService.getAllStates().size();

        long todayJobs = jobService.getAllJobs()
                .stream()
                .filter(job ->
                        job.getCreatedAt() != null &&
                                job.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        return new AdminDashboardResponse(
                totalJobs,
                totalDepartments,
                totalCategories,
                totalStates,
                todayJobs
        );
    }

    @Override
    public List<Job> getAllJobs() {
                return jobService.getAllJobs();
    }


            @Transactional
        @Override
        public Job createJob(JobRequest request) {
                Department department = departmentService.getDepartmentById(request.getDepartmentId())
                        .orElseThrow(() -> new EntityNotFoundException("Department not found"));

                Category category = categoryService.getCategoryById(request.getCategoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));

                State state = stateService.getStateById(request.getStateId())
                        .orElseThrow(() -> new EntityNotFoundException("State not found"));

                Job job = Job.builder()
            .title(request.getTitle())
            .qualification(request.getQualification())
            .vacancies(request.getVacancies())
            .salary(request.getSalary())
            .location(request.getLocation())
            .lastDate(request.getLastDate())
            .notificationUrl(request.getNotificationUrl())
            .applyUrl(request.getApplyUrl())
            .department(department)
            .category(category)
            .state(state)
            .build();
                return jobService.saveJob(job);
}

        @Transactional
        @Override
        public Job updateJob(Long id, JobRequest request) {

        Job job = jobService.getJobById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        Department department = departmentService.getDepartmentById(request.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        Category category = categoryService.getCategoryById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        State state = stateService.getStateById(request.getStateId())
                .orElseThrow(() -> new EntityNotFoundException("State not found"));

    job.setTitle(request.getTitle());
    job.setQualification(request.getQualification());
    job.setVacancies(request.getVacancies());
    job.setSalary(request.getSalary());
    job.setLocation(request.getLocation());
    job.setLastDate(request.getLastDate());
    job.setNotificationUrl(request.getNotificationUrl());
    job.setApplyUrl(request.getApplyUrl());

    job.setDepartment(department);
    job.setCategory(category);
    job.setState(state);

                return jobService.updateJob(id, job);
}

        @Transactional
        @Override
        public void deleteJob(Long id) {

                if (!jobService.getJobById(id).isPresent()) {
                        throw new EntityNotFoundException("Job not found");
                }

                jobService.deleteJob(id);
}


        @Transactional
        @Override
        public String bulkImport(BulkJobRequest request) {

    if (request == null || request.getJobs() == null || request.getJobs().isEmpty()) {
        throw new IllegalArgumentException("Job list cannot be empty.");
    }

    List<Job> jobs = new ArrayList<>();

        for (JobRequest dto : request.getJobs()) {

            Department department = departmentService.getDepartmentById(dto.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found : " + dto.getDepartmentId()));

            Category category = categoryService.getCategoryById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found : " + dto.getCategoryId()));

            State state = stateService.getStateById(dto.getStateId())
                    .orElseThrow(() -> new EntityNotFoundException("State not found : " + dto.getStateId()));

            Job job = Job.builder()
                .title(dto.getTitle())
                .qualification(dto.getQualification())
                .vacancies(dto.getVacancies())
                .salary(dto.getSalary())
                .location(dto.getLocation())
                .lastDate(dto.getLastDate())
                .notificationUrl(dto.getNotificationUrl())
                .applyUrl(dto.getApplyUrl())
                .department(department)
                .category(category)
                .state(state)
                .build();

        jobs.add(job);
    }

        for (Job j : jobs) {
                jobService.saveJob(j);
        }
        return "Bulk import successful. Total jobs imported: " + jobs.size();
}
}