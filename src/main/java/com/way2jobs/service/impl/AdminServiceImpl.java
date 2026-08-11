package com.way2jobs.service.impl;

import com.way2jobs.dto.AdminDashboardResponse;
import com.way2jobs.dto.AdminLoginRequest;
import com.way2jobs.dto.AdminLoginResponse;
import com.way2jobs.dto.BulkJobRequest;
import com.way2jobs.dto.JobRequest;
import com.way2jobs.entity.Category;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.State;
import com.way2jobs.security.JwtUtil;
import com.way2jobs.service.AdminService;
import com.way2jobs.service.CategoryService;
import com.way2jobs.service.DepartmentService;
import com.way2jobs.service.FirebaseMessagingService;
import com.way2jobs.service.JobService;
import com.way2jobs.service.StateService;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final JobService jobService;
    private final DepartmentService departmentService;
    private final CategoryService categoryService;
    private final StateService stateService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final JwtUtil jwtUtil;


    // ============================================================
    // ADMIN LOGIN
    // ============================================================

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {

        if (request == null) {
            return new AdminLoginResponse(
                    false,
                    "Invalid request",
                    null,
                    null
            );
        }

        if ("admin".equals(request.getUsername())
                && "admin123".equals(request.getPassword())) {

            return new AdminLoginResponse(
                    true,
                    "Login Successful",
                    jwtUtil.generateToken(
                            request.getUsername(),
                            "ADMIN"
                    ),
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


    // ============================================================
    // ADMIN DASHBOARD
    // ============================================================

    @Override
    public AdminDashboardResponse getDashboard() {

        List<Job> allJobs = jobService.getAllJobs();

        long totalJobs = allJobs.size();

        long totalDepartments =
                departmentService.getAllDepartments().size();

        long totalCategories =
                categoryService.getAllCategories().size();

        long totalStates =
                stateService.getAllStates().size();

        long todayJobs = allJobs
                .stream()
                .filter(job ->
                        job.getCreatedAt() != null
                                && job.getCreatedAt()
                                .toLocalDate()
                                .equals(LocalDate.now()))
                .count();

        return new AdminDashboardResponse(
                totalJobs,
                totalDepartments,
                totalCategories,
                totalStates,
                todayJobs
        );
    }


    // ============================================================
    // GET ALL JOBS
    // ============================================================

    @Override
    public List<Job> getAllJobs() {

        return jobService.getAllJobs();
    }


    // ============================================================
    // CREATE SINGLE JOB
    // ============================================================

    @Transactional
    @Override
    public Job createJob(JobRequest request) {

        validateJobRequest(request);

        Department department =
                departmentService
                        .getDepartmentById(request.getDepartmentId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Department not found: "
                                                + request.getDepartmentId()
                                )
                        );

        Category category =
                categoryService
                        .getCategoryById(request.getCategoryId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Category not found: "
                                                + request.getCategoryId()
                                )
                        );

        State state =
                stateService
                        .getStateById(request.getStateId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "State not found: "
                                                + request.getStateId()
                                )
                        );

        Job job = Job.builder()

                .title(request.getTitle())

                .qualification(
                        request.getQualification()
                )

                .vacancies(
                        request.getVacancies()
                )

                .salary(
                        request.getSalary()
                )

                .location(
                        request.getLocation()
                )

                .lastDate(
                        request.getLastDate()
                )

                .pdfNotification(
                        request.getNotificationUrl()
                )

                .applyLink(
                        request.getApplyUrl()
                )

                .category(
                        category.getName()
                )

                .state(
                        state.getName()
                )

                .build();

        return jobService.saveJob(job);
    }


    // ============================================================
    // UPDATE SINGLE JOB
    // ============================================================

    @Transactional
    @Override
    public Job updateJob(
            Long id,
            JobRequest request
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Job ID cannot be null"
            );
        }

        validateJobRequest(request);

        Job existingJob =
                jobService.getJobById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Job not found: " + id
                                )
                        );

        Category category =
                categoryService
                        .getCategoryById(request.getCategoryId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Category not found: "
                                                + request.getCategoryId()
                                )
                        );

        State state =
                stateService
                        .getStateById(request.getStateId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "State not found: "
                                                + request.getStateId()
                                )
                        );

        /*
         * Department is validated because JobRequest requires it.
         * Currently Job entity stores category/state as String,
         * so department is not directly stored inside Job.
         */
        departmentService
                .getDepartmentById(request.getDepartmentId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Department not found: "
                                        + request.getDepartmentId()
                        )
                );


        existingJob.setTitle(
                request.getTitle()
        );

        existingJob.setQualification(
                request.getQualification()
        );

        existingJob.setVacancies(
                request.getVacancies()
        );

        existingJob.setSalary(
                request.getSalary()
        );

        existingJob.setLocation(
                request.getLocation()
        );

        existingJob.setLastDate(
                request.getLastDate()
        );

        existingJob.setPdfNotification(
                request.getNotificationUrl()
        );

        existingJob.setApplyLink(
                request.getApplyUrl()
        );

        existingJob.setCategory(
                category.getName()
        );

        existingJob.setState(
                state.getName()
        );

        return jobService.updateJob(
                id,
                existingJob
        );
    }


    // ============================================================
    // DELETE JOB
    // ============================================================

    @Transactional
    @Override
    public void deleteJob(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Job ID cannot be null"
            );
        }

        if (!jobService.getJobById(id).isPresent()) {

            throw new EntityNotFoundException(
                    "Job not found: " + id
            );
        }

        jobService.deleteJob(id);
    }


    // ============================================================
    // BULK IMPORT
    //
    // NEW JOB
    //     -> INSERT
    //     -> Firebase notification
    //
    // EXISTING JOB
    //     -> UPDATE
    //     -> NO Firebase notification
    // ============================================================

    @Transactional
    @Override
    public String bulkImport(
            BulkJobRequest request
    ) {

        if (request == null
                || request.getJobs() == null
                || request.getJobs().isEmpty()) {

            throw new IllegalArgumentException(
                    "Job list cannot be empty."
            );
        }


        int insertedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        List<Job> newJobsForNotification =
                new ArrayList<>();


        // ========================================================
        // PROCESS EACH SCRAPER JOB
        // ========================================================

        for (JobRequest dto : request.getJobs()) {

            if (dto == null) {
                skippedCount++;
                continue;
            }

            validateJobRequest(dto);


            // ====================================================
            // VALIDATE DEPARTMENT
            // ====================================================

            Department department =
                    departmentService
                            .getDepartmentById(
                                    dto.getDepartmentId()
                            )
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "Department not found: "
                                                    + dto.getDepartmentId()
                                    )
                            );


            // ====================================================
            // VALIDATE CATEGORY
            // ====================================================

            Category category =
                    categoryService
                            .getCategoryById(
                                    dto.getCategoryId()
                            )
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "Category not found: "
                                                    + dto.getCategoryId()
                                    )
                            );


            // ====================================================
            // VALIDATE STATE
            // ====================================================

            State state =
                    stateService
                            .getStateById(
                                    dto.getStateId()
                            )
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "State not found: "
                                                    + dto.getStateId()
                                    )
                            );


            // ====================================================
            // FIND EXISTING JOB
            //
            // Priority:
            //
            // 1. Apply URL
            // 2. PDF notification
            // 3. Organization + Post Name + Last Date
            // ====================================================

            Optional<Job> existingJob =
                    findExistingJob(dto);


            // ====================================================
            // EXISTING JOB -> UPDATE
            // ====================================================

            if (existingJob.isPresent()) {

                Job oldJob =
                        existingJob.get();

                Job incomingJob =
                        buildJobFromRequest(
                                dto,
                                category,
                                state
                        );

                jobService.updateJob(
                        oldJob.getId(),
                        incomingJob
                );

                updatedCount++;

                continue;
            }


            // ====================================================
            // NEW JOB -> INSERT
            // ====================================================

            Job newJob =
                    buildJobFromRequest(
                            dto,
                            category,
                            state
                    );

            Job savedJob =
                    jobService.saveJob(newJob);

            if (savedJob != null
                    && savedJob.getId() != null) {

                insertedCount++;

                newJobsForNotification.add(
                        savedJob
                );
            }
        }


        // ========================================================
        // FIREBASE NOTIFICATION
        //
        // ONLY NEW JOBS
        // ========================================================

        for (Job savedJob :
                newJobsForNotification) {

            try {

                firebaseMessagingService.sendNotification(

                        "New Job Posted",

                        String.format(
                                "%s is now available. Apply now.",
                                savedJob.getTitle()
                        ),

                        savedJob.getId(),

                        Map.of(

                                "title",
                                savedJob.getTitle() == null
                                        ? "New Job"
                                        : savedJob.getTitle(),

                                "body",
                                savedJob.getLocation() == null
                                        ? "A new job is available"
                                        : "A new job is available in "
                                        + savedJob.getLocation(),

                                "jobId",
                                String.valueOf(
                                        savedJob.getId()
                                )
                        )
                );

            } catch (Exception e) {

                /*
                 * Firebase failure should NOT stop
                 * successful database import.
                 */

                System.err.println(
                        "Firebase notification failed for job "
                                + savedJob.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }


        // ========================================================
        // FINAL RESULT
        // ========================================================

        return String.format(

                "Bulk import completed. " +
                "Inserted: %d, Updated: %d, Skipped: %d",

                insertedCount,
                updatedCount,
                skippedCount
        );
    }


    // ============================================================
    // FIND EXISTING JOB
    // ============================================================

    private Optional<Job> findExistingJob(
            JobRequest dto
    ) {

        // --------------------------------------------------------
        // 1. CHECK APPLY LINK
        // --------------------------------------------------------

        if (dto.getApplyUrl() != null
                && !dto.getApplyUrl().isBlank()) {

            Optional<Job> byApplyLink =
                    jobService.findByApplyLink(
                            dto.getApplyUrl()
                    );

            if (byApplyLink.isPresent()) {

                return byApplyLink;
            }
        }


        // --------------------------------------------------------
        // 2. CHECK PDF NOTIFICATION
        // --------------------------------------------------------

        if (dto.getNotificationUrl() != null
                && !dto.getNotificationUrl().isBlank()) {

            Optional<Job> byPdf =
                    jobService.findByPdfNotification(
                            dto.getNotificationUrl()
                    );

            if (byPdf.isPresent()) {

                return byPdf;
            }
        }


        /*
         * --------------------------------------------------------
         * 3. NATURAL KEY
         *
         * Current JobRequest does NOT contain:
         *
         * organization
         * postName
         *
         * Therefore this check cannot normally run from the
         * current DTO.
         *
         * It is intentionally left disabled until those fields
         * are added to JobRequest.
         * --------------------------------------------------------
         */


        return Optional.empty();
    }


    // ============================================================
    // BUILD JOB FROM REQUEST
    // ============================================================

    private Job buildJobFromRequest(
            JobRequest dto,
            Category category,
            State state
    ) {

        return Job.builder()

                .title(
                        dto.getTitle()
                )

                .qualification(
                        dto.getQualification()
                )

                .vacancies(
                        dto.getVacancies()
                )

                .salary(
                        dto.getSalary()
                )

                .location(
                        dto.getLocation()
                )

                .lastDate(
                        dto.getLastDate()
                )

                .pdfNotification(
                        dto.getNotificationUrl()
                )

                .applyLink(
                        dto.getApplyUrl()
                )

                .category(
                        category.getName()
                )

                .state(
                        state.getName()
                )

                .build();
    }


    // ============================================================
    // REQUEST VALIDATION
    // ============================================================

    private void validateJobRequest(
            JobRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Job request cannot be null"
            );
        }


        if (request.getTitle() == null
                || request.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Job title cannot be empty"
            );
        }


        if (request.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department ID is required"
            );
        }


        if (request.getCategoryId() == null) {

            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }


        if (request.getStateId() == null) {

            throw new IllegalArgumentException(
                    "State ID is required"
            );
        }
    }
}