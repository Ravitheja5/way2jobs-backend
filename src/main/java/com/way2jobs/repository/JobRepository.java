package com.way2jobs.repository;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

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

    /*
     * Find existing job by application URL.
     */
    Optional<Job> findFirstByApplyLinkIgnoreCase(
            String applyLink
    );

    /*
     * Find existing job by notification PDF URL.
     */
    Optional<Job> findFirstByPdfNotificationIgnoreCase(
            String pdfNotification
    );

    /*
     * Natural-key fallback:
     *
     * organization + postName + lastDate
     */
    Optional<Job> findFirstByOrganizationIgnoreCaseAndPostNameIgnoreCaseAndLastDate(
            String organization,
            String postName,
            LocalDate lastDate
    );
}