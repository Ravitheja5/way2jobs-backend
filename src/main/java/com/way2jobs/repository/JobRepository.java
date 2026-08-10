package com.way2jobs.repository;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JobRepository extends JpaRepository<Job, Long> {

    // =========================
    // STATE FILTERING
    // =========================

    Page<Job> findByStateIgnoreCaseAndIsActiveTrue(
            String state,
            Pageable pageable
    );

    Page<Job> findByStateIgnoreCase(
            String state,
            Pageable pageable
    );

    Page<Job> findByState(
            String state,
            Pageable pageable
    );

    // =========================
    // DUPLICATE CHECKS
    // =========================

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
}