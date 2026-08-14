package com.way2jobs.repository;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    // NEW — MOST VIEWED
    // ============================================================

    Page<Job> findByIsActiveTrueOrderByViewCountDesc(
            Pageable pageable
    );


    // ============================================================
    // NEW — LATEST IMPORTED JOBS
    // ============================================================

    Page<Job> findByIsActiveTrueOrderByImportedAtDesc(
            Pageable pageable
    );


    // ============================================================
    // NEW — EXPIRING SOON
    // ============================================================
    //
    // Active jobs whose last date is:
    //
    // today <= lastDate <= today + 6 days
    //
    // Expired jobs are automatically excluded.
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
    // NEW — FILTER COUNTS
    // ============================================================

    long countByIsActiveTrue();

    long countByIsActiveTrueAndStateIgnoreCase(
            String state
    );

    long countByIsActiveTrueAndLastDateGreaterThanEqualAndLastDateLessThanEqual(
            LocalDate today,
            LocalDate expiryDate
    );
}