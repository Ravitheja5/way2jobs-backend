package com.way2jobs.repository;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    // ============================================================
    // EXISTING STATE QUERIES
    // ============================================================

    Page<Job> findByStateIgnoreCaseAndIsActiveTrue(
            String state,
            Pageable pageable
    );

    Page<Job> findByStateIgnoreCase(
            String state,
            Pageable pageable
    );

    Page<Job> findByStateContainingIgnoreCase(
            String state,
            Pageable pageable
    );


    // ============================================================
    // EXISTING DUPLICATE CHECKS
    // ============================================================

    boolean existsByPdfNotificationIgnoreCase(
            String pdfNotification
    );

    boolean existsByApplyLinkIgnoreCase(
            String applyLink
    );

    boolean existsByTitleIgnoreCaseAndLocationIgnoreCaseAndLastDateAndCategoryIgnoreCaseAndStateIgnoreCase(
            String title,
            String location,
            LocalDate lastDate,
            String category,
            String state
    );


    // ============================================================
    // EXISTING NATURAL-KEY LOOKUPS
    // ============================================================

    Optional<Job> findFirstByApplyLinkIgnoreCase(
            String applyLink
    );

    Optional<Job> findFirstByPdfNotificationIgnoreCase(
            String pdfNotification
    );

    Optional<Job> findFirstByOrganizationIgnoreCaseAndPostNameIgnoreCaseAndLastDate(
            String organization,
            String postName,
            LocalDate lastDate
    );


    // ============================================================
    // MOST VIEWED
    // ============================================================

    Page<Job> findByIsActiveTrueOrderByViewCountDesc(
            Pageable pageable
    );

    // ============================================================
    // LATEST JOBS
    // ============================================================
    //
    // IMPORTANT:
    // Latest Jobs is based on POST DATE.
    //
    // Rules:
    //
    // isActive = true
    // postDate >= today - 6 days
    // postDate <= today
    //
    // Ordered by newest postDate first.
    //

    @Query("""
            SELECT j
            FROM Job j
            WHERE j.isActive = true
              AND j.postDate IS NOT NULL
              AND j.postDate >= :startDate
              AND j.postDate <= :endDate
            ORDER BY j.postDate DESC
            """)
    Page<Job> findLatestJobs(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );


    // ============================================================
    // EXPIRING SOON
    // ============================================================
    //
    // Active jobs whose last date is:
    //
    // today <= lastDate <= today + 6 days
    //
    // Expired jobs are excluded.
    //

    @Query("""
            SELECT j
            FROM Job j
            WHERE j.isActive = true
              AND j.lastDate IS NOT NULL
              AND j.lastDate >= :today
              AND j.lastDate <= :expiryDate
            ORDER BY j.lastDate ASC
            """)
    Page<Job> findExpiringSoon(
            @Param("today") LocalDate today,
            @Param("expiryDate") LocalDate expiryDate,
            Pageable pageable
    );


    // ============================================================
    // FILTER COUNTS
    // ============================================================

    long countByIsActiveTrue();

    long countByIsActiveTrueAndStateIgnoreCase(
            String state
    );

    long countByIsActiveTrueAndLastDateGreaterThanEqualAndLastDateLessThanEqual(
            LocalDate today,
            LocalDate expiryDate
    );


    // ============================================================
    // STATE-WISE CURRENT JOB COUNTS
    // ============================================================
    //
    // Returns:
    //
    // state + number of active jobs
    //
    // Example:
    //
    // Andhra Pradesh -> 120
    // Telangana      -> 95
    // Karnataka      -> 80
    //

    @Query("""
            SELECT j.state, COUNT(j)
            FROM Job j
            WHERE j.isActive = true
              AND j.state IS NOT NULL
              AND TRIM(j.state) <> ''
            GROUP BY j.state
            ORDER BY COUNT(j) DESC
            """)
    List<Object[]> findActiveJobCountsByState();
}