package com.way2jobs.service;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobService {

    // ============================================================
    // EXISTING CRUD
    // ============================================================

    Job saveJob(Job job);

    List<Job> getAllJobs();

    Optional<Job> getJobById(Long id);

    Job updateJob(Long id, Job job);

    void deleteJob(Long id);


    // ============================================================
    // EXISTING STATE FILTERING
    // ============================================================

    List<Job> getJobsByState(String state);

    List<Job> getAllIndiaJobs();

    Page<Job> getJobs(Pageable pageable);

    Page<Job> getJobsByState(
            String state,
            Pageable pageable
    );

    Page<Job> getAllIndiaJobs(
            Pageable pageable
    );


    // ============================================================
    // EXISTING BULK IMPORT DUPLICATE DETECTION
    // ============================================================

    Optional<Job> findByApplyLink(
            String applyLink
    );

    Optional<Job> findByPdfNotification(
            String pdfNotification
    );

    Optional<Job> findByNaturalKey(
            String organization,
            String postName,
            LocalDate lastDate
    );


    // ============================================================
    // NEW — MOST VIEWED
    // ============================================================

    Page<Job> getMostViewedJobs(
            Pageable pageable
    );


    // ============================================================
    // NEW — LATEST IMPORTED JOBS
    // ============================================================

    Page<Job> getLatestJobs(
            Pageable pageable
    );


    // ============================================================
    // NEW — EXPIRING SOON
    // ============================================================

    Page<Job> getExpiringSoonJobs(
            Pageable pageable
    );


    // ============================================================
    // NEW — VIEW COUNT
    // ============================================================

    Job incrementViewCount(
            Long jobId
    );


    // ============================================================
    // NEW — LIKE COUNT
    // ============================================================

    Job incrementLikeCount(
            Long jobId
    );

    Job decrementLikeCount(
            Long jobId
    );


    // ============================================================
    // NEW — DAYS LEFT
    // ============================================================

    long getDaysLeft(
            Job job
    );


    // ============================================================
    // NEW — SOURCE PRIORITY
    // ============================================================

    String getSourceLabel(
            Job job
    );

    String getSourceUrl(
            Job job
    );


    // ============================================================
    // NEW — FILTER COUNTS
    // ============================================================

    long getTotalActiveJobs();

    long getStateJobCount(
            String state
    );

    List<Object[]> getActiveJobCountsByState();

    long getExpiringSoonJobCount();
}