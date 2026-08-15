package com.way2jobs.service.impl;

import com.way2jobs.entity.Job;
import com.way2jobs.repository.JobRepository;
import com.way2jobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;


    // ============================================================
    // EXISTING CRUD
    // ============================================================

    @Override
    public Job saveJob(Job job) {

        return jobRepository.save(job);
    }


    @Override
    public List<Job> getAllJobs() {

        return jobRepository.findAll();
    }


    @Override
    public Optional<Job> getJobById(Long id) {

        return jobRepository.findById(id);
    }


    @Override
    public Job updateJob(Long id, Job job) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        )
                );

        if (job.getJobId() != null &&
                !job.getJobId().isBlank()) {

            existingJob.setJobId(job.getJobId());
        }

        if (job.getTitle() != null &&
                !job.getTitle().isBlank()) {

            existingJob.setTitle(job.getTitle());
        }

        if (job.getOrganization() != null &&
                !job.getOrganization().isBlank()) {

            existingJob.setOrganization(job.getOrganization());
        }

        if (job.getPostName() != null &&
                !job.getPostName().isBlank()) {

            existingJob.setPostName(job.getPostName());
        }

        if (job.getVacancies() != null) {

            existingJob.setVacancies(job.getVacancies());
        }

        if (job.getQualification() != null &&
                !job.getQualification().isBlank()) {

            existingJob.setQualification(job.getQualification());
        }

        if (job.getSalary() != null &&
                !job.getSalary().isBlank()) {

            existingJob.setSalary(job.getSalary());
        }

        if (job.getLocation() != null &&
                !job.getLocation().isBlank()) {

            existingJob.setLocation(job.getLocation());
        }

        if (job.getLastDate() != null) {

            existingJob.setLastDate(job.getLastDate());
        }

        if (job.getApplyLink() != null &&
                !job.getApplyLink().isBlank()) {

            existingJob.setApplyLink(job.getApplyLink());
        }

        if (job.getPdfNotification() != null &&
                !job.getPdfNotification().isBlank()) {

            existingJob.setPdfNotification(
                    job.getPdfNotification()
            );
        }

        if (job.getOfficialWebsite() != null &&
                !job.getOfficialWebsite().isBlank()) {

            existingJob.setOfficialWebsite(
                    job.getOfficialWebsite()
            );
        }

        if (job.getPostDate() != null) {

            existingJob.setPostDate(
                    job.getPostDate()
            );
        }

        if (job.getCategory() != null &&
                !job.getCategory().isBlank()) {

            existingJob.setCategory(job.getCategory());
        }

        if (job.getState() != null &&
                !job.getState().isBlank()) {

            existingJob.setState(job.getState());
        }

        if (job.getSelectionProcess() != null &&
                !job.getSelectionProcess().isBlank()) {

            existingJob.setSelectionProcess(
                    job.getSelectionProcess()
            );
        }

        if (job.getAgeLimit() != null &&
                !job.getAgeLimit().isBlank()) {

            existingJob.setAgeLimit(
                    job.getAgeLimit()
            );
        }

        if (job.getApplicationFee() != null &&
                !job.getApplicationFee().isBlank()) {

            existingJob.setApplicationFee(
                    job.getApplicationFee()
            );
        }

        if (job.getExperience() != null &&
                !job.getExperience().isBlank()) {

            existingJob.setExperience(
                    job.getExperience()
            );
        }

        if (job.getIsActive() != null) {

            existingJob.setIsActive(
                    job.getIsActive()
            );
        }

        if (job.getSource() != null &&
                !job.getSource().isBlank()) {

            existingJob.setSource(job.getSource());
        }

        // --------------------------------------------------------
        // IMPORTANT
        // --------------------------------------------------------
        // Do NOT update:
        //
        // viewCount
        // likeCount
        // importedAt
        //
        // automatically during normal job updates.
        //
        // User engagement data must be preserved.
        // --------------------------------------------------------

        return jobRepository.save(existingJob);
    }


    @Override
    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {

            throw new RuntimeException(
                    "Job not found with id: " + id
            );
        }

        jobRepository.deleteById(id);
    }


    // ============================================================
    // STATE FILTERING
    // ============================================================

    @Override
    public List<Job> getJobsByState(String state) {

        return jobRepository.findByStateIgnoreCase(
                state,
                Pageable.unpaged()
        ).getContent();
    }


    @Override
    public List<Job> getAllIndiaJobs() {

        return jobRepository.findByStateIgnoreCase(
                "All India",
                Pageable.unpaged()
        ).getContent();
    }


    @Override
    public Page<Job> getJobs(Pageable pageable) {

        return jobRepository.findAll(pageable);
    }


    @Override
    public Page<Job> getJobsByState(
            String state,
            Pageable pageable
    ) {

        if (state == null ||
                state.isBlank() ||
                "All India".equalsIgnoreCase(
                        state.trim()
                )) {

            return getAllIndiaJobs(pageable);
        }

        String normalizedState = state.trim();

        return jobRepository.findByStateContainingIgnoreCase(
                normalizedState,
                pageable
        );
    }


    @Override
    public Page<Job> getAllIndiaJobs(
            Pageable pageable
    ) {

        return jobRepository.findByStateIgnoreCase(
                "All India",
                pageable
        );
    }


    // ============================================================
    // BULK IMPORT DUPLICATE DETECTION
    // ============================================================

    @Override
    public Optional<Job> findByApplyLink(
            String applyLink
    ) {

        if (applyLink == null ||
                applyLink.isBlank()) {

            return Optional.empty();
        }

        return jobRepository.findFirstByApplyLinkIgnoreCase(
                applyLink.trim()
        );
    }


    @Override
    public Optional<Job> findByPdfNotification(
            String pdfNotification
    ) {

        if (pdfNotification == null ||
                pdfNotification.isBlank()) {

            return Optional.empty();
        }

        return jobRepository.findFirstByPdfNotificationIgnoreCase(
                pdfNotification.trim()
        );
    }


    @Override
    public Optional<Job> findByNaturalKey(
            String organization,
            String postName,
            LocalDate lastDate
    ) {

        if (organization == null ||
                organization.isBlank() ||
                postName == null ||
                postName.isBlank() ||
                lastDate == null) {

            return Optional.empty();
        }

        return jobRepository
                .findFirstByOrganizationIgnoreCaseAndPostNameIgnoreCaseAndLastDate(
                        organization.trim(),
                        postName.trim(),
                        lastDate
                );
    }


    // ============================================================
    // NEW — MOST VIEWED JOBS
    // ============================================================

    @Override
    public Page<Job> getMostViewedJobs(
            Pageable pageable
    ) {

        return jobRepository
                .findByIsActiveTrueOrderByViewCountDesc(
                        pageable
                );
    }


    // ============================================================
    // NEW — LATEST IMPORTED JOBS
    // ============================================================

    @Override
    public Page<Job> getLatestJobs(
            Pageable pageable
    ) {

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(6);

        return jobRepository.findLatestJobs(
                startDate,
                endDate,
                pageable
        );
    }


    // ============================================================
    // NEW — EXPIRING SOON
    // ============================================================

    @Override
    public Page<Job> getExpiringSoonJobs(
            Pageable pageable
    ) {

        LocalDate today = LocalDate.now();

        LocalDate expiryDate = today.plusDays(6);

        return jobRepository.findExpiringSoon(
                today,
                expiryDate,
                pageable
        );
    }


    // ============================================================
    // NEW — VIEW COUNT
    // ============================================================

    @Override
    @Transactional
    public Job incrementViewCount(
            Long jobId
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + jobId
                        )
                );

        Long currentViews = job.getViewCount();

        if (currentViews == null) {
            currentViews = 0L;
        }

        job.setViewCount(
                currentViews + 1
        );

        return jobRepository.save(job);
    }


    // ============================================================
    // NEW — LIKE COUNT
    // ============================================================

    @Override
    @Transactional
    public Job incrementLikeCount(
            Long jobId
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + jobId
                        )
                );

        Long currentLikes = job.getLikeCount();

        if (currentLikes == null) {
            currentLikes = 0L;
        }

        job.setLikeCount(
                currentLikes + 1
        );

        return jobRepository.save(job);
    }


    // ============================================================
    // NEW — UNLIKE / DECREASE LIKE COUNT
    // ============================================================

    @Override
    @Transactional
    public Job decrementLikeCount(
            Long jobId
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + jobId
                        )
                );

        Long currentLikes = job.getLikeCount();

        if (currentLikes == null ||
                currentLikes <= 0) {

            job.setLikeCount(0L);

        } else {

            job.setLikeCount(
                    currentLikes - 1
            );
        }

        return jobRepository.save(job);
    }


    // ============================================================
    // NEW — DAYS LEFT
    // ============================================================

    @Override
    public long getDaysLeft(
            Job job
    ) {

        if (job == null ||
                job.getLastDate() == null) {

            return -1;
        }

        LocalDate today = LocalDate.now();

        return ChronoUnit.DAYS.between(
                today,
                job.getLastDate()
        );
    }


    // ============================================================
    // NEW — SOURCE PRIORITY
    // ============================================================

    @Override
    public String getSourceLabel(
            Job job
    ) {

        if (job == null) {
            return null;
        }

        if (hasValue(job.getOfficialWebsite())) {
            return "Official Website";
        }

        if (hasValue(job.getApplyLink())) {
            return "Apply";
        }

        if (hasValue(job.getPdfNotification())) {
            return "Notification";
        }

        return null;
    }


    @Override
    public String getSourceUrl(
            Job job
    ) {

        if (job == null) {
            return null;
        }

        if (hasValue(job.getOfficialWebsite())) {
            return job.getOfficialWebsite();
        }

        if (hasValue(job.getApplyLink())) {
            return job.getApplyLink();
        }

        if (hasValue(job.getPdfNotification())) {
            return job.getPdfNotification();
        }

        return null;
    }


    // ============================================================
    // NEW — FILTER COUNTS
    // ============================================================

    @Override
    public long getTotalActiveJobs() {

        return jobRepository.countByIsActiveTrue();
    }


    @Override
    public long getStateJobCount(
            String state
    ) {

        if (state == null ||
                state.isBlank()) {

            return getTotalActiveJobs();
        }

        return jobRepository.countByIsActiveTrueAndStateIgnoreCase(
                state.trim()
        );
    }


    @Override
    public long getExpiringSoonJobCount() {

        LocalDate today = LocalDate.now();

        LocalDate expiryDate = today.plusDays(6);

        return jobRepository
                .countByIsActiveTrueAndLastDateGreaterThanEqualAndLastDateLessThanEqual(
                        today,
                        expiryDate
                );
    }


    // ============================================================
    // HELPER
    // ============================================================

    private boolean hasValue(
            String value
    ) {

        return value != null &&
                !value.isBlank();
    }

    @Override
    public List<Object[]> getActiveJobCountsByState() {

        return jobRepository.findActiveJobCountsByState();
    }

   
}