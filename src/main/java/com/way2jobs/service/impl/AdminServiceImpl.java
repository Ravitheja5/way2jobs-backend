package com.way2jobs.service.impl;

import com.way2jobs.dto.AdminDashboardResponse;
import com.way2jobs.dto.AdminLoginRequest;
import com.way2jobs.dto.AdminLoginResponse;
import com.way2jobs.dto.BulkJobItem;
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
    public AdminLoginResponse login(
            AdminLoginRequest request
    ) {

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

        List<Job> allJobs =
                jobService.getAllJobs();

        long totalJobs =
                allJobs.size();

        long totalDepartments =
                departmentService
                        .getAllDepartments()
                        .size();

        long totalCategories =
                categoryService
                        .getAllCategories()
                        .size();

        long totalStates =
                stateService
                        .getAllStates()
                        .size();

        long todayJobs =
                allJobs
                        .stream()
                        .filter(job ->
                                job.getCreatedAt() != null
                                        && job.getCreatedAt()
                                        .toLocalDate()
                                        .equals(LocalDate.now())
                        )
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
    public Job createJob(
            JobRequest request
    ) {

        validateJobRequest(request);

        Category category =
                categoryService
                        .getCategoryById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Category not found: "
                                                + request.getCategoryId()
                                )
                        );

        State state =
                stateService
                        .getStateById(
                                request.getStateId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "State not found: "
                                                + request.getStateId()
                                )
                        );

        departmentService
                .getDepartmentById(
                        request.getDepartmentId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Department not found: "
                                        + request.getDepartmentId()
                        )
                );

        Job job =
                buildJobFromRequest(
                        request,
                        category,
                        state
                );

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
                jobService
                        .getJobById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Job not found: " + id
                                )
                        );

        Category category =
                categoryService
                        .getCategoryById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Category not found: "
                                                + request.getCategoryId()
                                )
                        );

        State state =
                stateService
                        .getStateById(
                                request.getStateId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "State not found: "
                                                + request.getStateId()
                                )
                        );

        departmentService
                .getDepartmentById(
                        request.getDepartmentId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Department not found: "
                                        + request.getDepartmentId()
                        )
                );

        existingJob.setTitle(
                request.getTitle()
        );

        existingJob.setOrganization(
                request.getOrganization()
        );

        existingJob.setPostName(
                request.getPostName()
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

        existingJob.setOfficialWebsite(
                request.getOfficialWebsite()
        );

        existingJob.setAgeLimit(
                request.getAgeLimit()
        );

        existingJob.setExperience(
                request.getExperience()
        );

        existingJob.setApplicationFee(
                request.getApplicationFee()
        );

        existingJob.setSelectionProcess(
                request.getSelectionProcess()
        );

        if (request.getPostDate() != null) {

            existingJob.setPostDate(
                    request.getPostDate()
                            .atStartOfDay()
            );
        }

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

        if (jobService.getJobById(id).isEmpty()) {

            throw new EntityNotFoundException(
                    "Job not found: " + id
            );
        }

        jobService.deleteJob(id);
    }


    // ============================================================
    // SOURCE-DRIVEN BULK IMPORT
    //
    // IMPORTANT:
    //
    // Python sends:
    //
    // organization
    // category
    // state
    //
    // Python does NOT send:
    //
    // departmentId
    // categoryId
    // stateId
    //
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


        // ========================================================
        // PROCESS EVERY JOB
        // ========================================================

        for (BulkJobItem dto :
                request.getJobs()) {

            if (dto == null) {

                skippedCount++;

                continue;
            }

            try {

                // ------------------------------------------------
                // VALIDATE
                // ------------------------------------------------

                validateBulkJobItem(dto);


                // ------------------------------------------------
                // RESOLVE ORGANIZATION
                // ------------------------------------------------

                resolveOrCreateDepartment(
                        dto.getOrganization(),
                        dto.getOfficialWebsite()
                );


                // ------------------------------------------------
                // RESOLVE CATEGORY
                // ------------------------------------------------

                Category category =
                        resolveOrCreateCategory(
                                dto.getCategory()
                        );


                // ------------------------------------------------
                // RESOLVE STATE
                // ------------------------------------------------

                State state =
                        resolveState(
                                dto.getState()
                        );


                // ------------------------------------------------
                // DUPLICATE DETECTION
                // ------------------------------------------------

                Optional<Job> existingJob =
                        findExistingBulkJob(dto);


                // =================================================
                // UPDATE
                // =================================================

                if (existingJob.isPresent()) {

                    Job incomingJob =
                            buildJobFromBulkItem(
                                    dto,
                                    category,
                                    state
                            );

                    jobService.updateJob(
                            existingJob.get().getId(),
                            incomingJob
                    );

                    updatedCount++;

                    continue;
                }


                // =================================================
                // INSERT
                // =================================================

                Job newJob =
                        buildJobFromBulkItem(
                                dto,
                                category,
                                state
                        );

                Job savedJob =
                        jobService.saveJob(
                                newJob
                        );

                if (savedJob != null
                        && savedJob.getId() != null) {

                    insertedCount++;
                }

            } catch (Exception e) {

                skippedCount++;

                System.err.println(
                        "Skipping bulk job: "
                                + safeTitle(dto)
                                + " | reason: "
                                + e.getMessage()
                );
            }
        }


        // ========================================================
        // IMPORTANT PERFORMANCE CHANGE
        // ========================================================
        //
        // Firebase notifications are NOT sent synchronously here.
        //
        // Previously the importer waited for Firebase notification
        // processing for every newly inserted job before returning
        // the HTTP response.
        //
        // The bulk importer must finish the database operation and
        // return the result to the scraper as quickly as possible.
        //
        // Notifications can be handled separately later.
        // ========================================================


        return String.format(
                "Bulk import completed. "
                        + "Inserted: %d, Updated: %d, Skipped: %d",
                insertedCount,
                updatedCount,
                skippedCount
        );
    }


    // ============================================================
    // ORGANIZATION / DEPARTMENT RESOLUTION
    // ============================================================

    private Department resolveOrCreateDepartment(
            String organization,
            String officialWebsite
    ) {

        String normalizedName =
                normalizeSourceName(
                        organization
                );

        Optional<Department> existing =
                departmentService
                        .getDepartmentByName(
                                normalizedName
                        );

        if (existing.isPresent()) {

            Department department =
                    existing.get();

            if ((department.getOfficialWebsite() == null
                    || department.getOfficialWebsite().isBlank())
                    && officialWebsite != null
                    && !officialWebsite.isBlank()) {

                department.setOfficialWebsite(
                        officialWebsite.trim()
                );

                department =
                        departmentService.saveDepartment(
                                department
                        );
            }

            return department;
        }


        Department department =
                Department.builder()
                        .name(normalizedName)
                        .shortName(
                                generateUniqueShortName(
                                        normalizedName
                                )
                        )
                        .officialWebsite(
                                blankToNull(
                                        officialWebsite
                                )
                        )
                        .build();

        return departmentService.saveDepartment(
                department
        );
    }


    // ============================================================
    // CATEGORY RESOLUTION
    // ============================================================

    private Category resolveOrCreateCategory(
            String categoryName
    ) {

        String normalized =
                normalizeSourceName(
                        categoryName
                );

        Optional<Category> existing =
                categoryService
                        .getCategoryByName(
                                normalized
                        );

        if (existing.isPresent()) {

            return existing.get();
        }


        Category category =
                Category.builder()
                        .name(normalized)
                        .build();

        return categoryService.saveCategory(
                category
        );
    }


    // ============================================================
    // STATE RESOLUTION
    // ============================================================

    private State resolveState(
            String stateName
    ) {

        String normalized =
                normalizeSourceName(
                        stateName
                );

        Optional<State> exact =
                stateService.getStateByName(
                        normalized
                );

        if (exact.isPresent()) {

            return exact.get();
        }


        String target =
                normalizeComparable(
                        normalized
                );

        return stateService
                .getAllStates()
                .stream()
                .filter(state ->
                        normalizeComparable(
                                state.getName()
                        ).equals(target)
                )
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Source state not found in "
                                        + "state master data: "
                                        + stateName
                        )
                );
    }


    // ============================================================
    // DUPLICATE DETECTION
    // ============================================================

    private Optional<Job> findExistingBulkJob(
            BulkJobItem dto
    ) {

        // --------------------------------------------------------
        // 1. APPLY URL
        // --------------------------------------------------------

        if (dto.getApplyUrl() != null
                && !dto.getApplyUrl().isBlank()) {

            Optional<Job> byApply =
                    jobService.findByApplyLink(
                            dto.getApplyUrl()
                    );

            if (byApply.isPresent()) {

                return byApply;
            }
        }


        // --------------------------------------------------------
        // 2. NOTIFICATION PDF
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


        // --------------------------------------------------------
        // 3. NATURAL KEY
        // --------------------------------------------------------

        if (dto.getOrganization() != null
                && !dto.getOrganization().isBlank()
                && dto.getPostName() != null
                && !dto.getPostName().isBlank()
                && dto.getLastDate() != null) {

            Optional<Job> byNaturalKey =
                    jobService.findByNaturalKey(
                            dto.getOrganization(),
                            dto.getPostName(),
                            dto.getLastDate()
                    );

            if (byNaturalKey.isPresent()) {

                return byNaturalKey;
            }
        }

        return Optional.empty();
    }


    // ============================================================
    // BUILD JOB FROM BULK ITEM
    // ============================================================

    private Job buildJobFromBulkItem(
            BulkJobItem dto,
            Category category,
            State state
    ) {

        Job.JobBuilder builder =
                Job.builder()

                        // ----------------------------------------
                        // SOURCE IDENTITY
                        // ----------------------------------------

                        .jobId(
                                dto.getJobId()
                        )

                        .source(
                                dto.getSource()
                        )

                        // ----------------------------------------
                        // JOB DATA
                        // ----------------------------------------

                        .title(
                                dto.getTitle()
                        )

                        .organization(
                                dto.getOrganization()
                        )

                        .postName(
                                dto.getPostName()
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

                        // ----------------------------------------
                        // LINKS
                        // ----------------------------------------

                        .pdfNotification(
                                dto.getNotificationUrl()
                        )

                        .applyLink(
                                dto.getApplyUrl()
                        )

                        .officialWebsite(
                                dto.getOfficialWebsite()
                        )

                        // ----------------------------------------
                        // ELIGIBILITY
                        // ----------------------------------------

                        .ageLimit(
                                dto.getAgeLimit()
                        )

                        .experience(
                                dto.getExperience()
                        )

                        .applicationFee(
                                dto.getApplicationFee()
                        )

                        .selectionProcess(
                                dto.getSelectionProcess()
                        )

                        // ----------------------------------------
                        // MASTER VALUES
                        // ----------------------------------------

                        .category(
                                category.getName()
                        )

                        .state(
                                state.getName()
                        );


        // --------------------------------------------------------
        // POST DATE
        // --------------------------------------------------------

        if (dto.getPostDate() != null) {

            builder.postDate(
                    dto.getPostDate()
                            .atStartOfDay()
            );
        }

        return builder.build();
    }


    // ============================================================
    // BULK VALIDATION
    // ============================================================

    private void validateBulkJobItem(
            BulkJobItem dto
    ) {

        if (dto.getTitle() == null
                || dto.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Source job title is missing."
            );
        }

        if (dto.getOrganization() == null
                || dto.getOrganization().isBlank()) {

            throw new IllegalArgumentException(
                    "Source organization is missing."
            );
        }

        if (dto.getCategory() == null
                || dto.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "Source category is missing."
            );
        }

        if (dto.getState() == null
                || dto.getState().isBlank()) {

            throw new IllegalArgumentException(
                    "Source state is missing."
            );
        }
    }


    // ============================================================
    // MANUAL ADMIN JOB VALIDATION
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


    // ============================================================
    // MANUAL ADMIN JOB BUILDER
    // ============================================================

    private Job buildJobFromRequest(
            JobRequest dto,
            Category category,
            State state
    ) {

        Job.JobBuilder builder =
                Job.builder()
                        .title(
                                dto.getTitle()
                        )
                        .organization(
                                dto.getOrganization()
                        )
                        .postName(
                                dto.getPostName()
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
                        .officialWebsite(
                                dto.getOfficialWebsite()
                        )
                        .ageLimit(
                                dto.getAgeLimit()
                        )
                        .experience(
                                dto.getExperience()
                        )
                        .applicationFee(
                                dto.getApplicationFee()
                        )
                        .selectionProcess(
                                dto.getSelectionProcess()
                        )
                        .category(
                                category.getName()
                        )
                        .state(
                                state.getName()
                        );


        if (dto.getPostDate() != null) {

            builder.postDate(
                    dto.getPostDate()
                            .atStartOfDay()
            );
        }

        return builder.build();
    }


    // ============================================================
    // TEXT NORMALIZATION
    // ============================================================

    private String normalizeSourceName(
            String value
    ) {

        if (value == null) {

            return null;
        }

        return value
                .replace('\u00A0', ' ')
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    private String normalizeComparable(
            String value
    ) {

        if (value == null) {

            return "";
        }

        return value
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );
    }


    private String blankToNull(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }


    // ============================================================
    // AUTOMATIC DEPARTMENT SHORT NAME
    // ============================================================

    private String generateUniqueShortName(
            String organization
    ) {

        String cleaned =
                organization
                        .replaceAll(
                                "[^A-Za-z0-9 ]",
                                " "
                        )
                        .trim();


        if (cleaned.isBlank()) {

            cleaned = "ORG";
        }


        String[] words =
                cleaned.split("\\s+");


        StringBuilder builder =
                new StringBuilder();


        for (String word : words) {

            if (!word.isBlank()) {

                builder.append(
                        Character.toUpperCase(
                                word.charAt(0)
                        )
                );
            }
        }


        String base =
                builder.length() > 0
                        ? builder.toString()
                        : "ORG";


        if (base.length() > 45) {

            base =
                    base.substring(
                            0,
                            45
                    );
        }


        String candidate =
                base;

        int suffix = 2;


        while (
                departmentService
                        .getDepartmentByShortName(
                                candidate
                        )
                        .isPresent()
        ) {

            String suffixText =
                    String.valueOf(
                            suffix++
                    );

            int maxBaseLength =
                    50
                            - suffixText.length();


            String shortenedBase =
                    base.length() > maxBaseLength
                            ? base.substring(
                                    0,
                                    maxBaseLength
                            )
                            : base;


            candidate =
                    shortenedBase
                            + suffixText;
        }


        return candidate;
    }


    // ============================================================
    // SAFE LOGGING
    // ============================================================

    private String safeTitle(
            BulkJobItem dto
    ) {

        if (dto == null
                || dto.getTitle() == null) {

            return "UNKNOWN";
        }

        return dto.getTitle();
    }
}