package com.way2jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // BASIC JOB IDENTIFICATION
    // ============================================================

    @Column(name = "job_id", length = 255)
    private String jobId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "organization", columnDefinition = "TEXT")
    private String organization;

    @Column(name = "post_name", columnDefinition = "TEXT")
    private String postName;

    @Column(name = "vacancies")
    private Integer vacancies;


    // ============================================================
    // JOB DETAILS
    // ============================================================

    @Column(name = "qualification", columnDefinition = "TEXT")
    private String qualification;

    @Column(name = "salary", columnDefinition = "TEXT")
    private String salary;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "age_limit", columnDefinition = "TEXT")
    private String ageLimit;

    @Column(name = "application_fee", columnDefinition = "TEXT")
    private String applicationFee;

    @Column(name = "selection_process", columnDefinition = "TEXT")
    private String selectionProcess;


    // ============================================================
    // DATES
    // ============================================================

    @Column(name = "last_date")
    private LocalDate lastDate;

    @Column(name = "post_date")
    private LocalDateTime postDate;


    // ============================================================
    // LINKS
    // ============================================================

    @Column(name = "apply_link", columnDefinition = "TEXT")
    private String applyLink;

    @Column(name = "pdf_notification", columnDefinition = "TEXT")
    private String pdfNotification;

    @Column(name = "official_website", columnDefinition = "TEXT")
    private String officialWebsite;


    // ============================================================
    // CLASSIFICATION
    // ============================================================

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "source", length = 100)
    private String source;


    // ============================================================
    // STATUS / AUDIT
    // ============================================================

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    // ============================================================
    // NEW — USER ENGAGEMENT
    // ============================================================

    /**
     * Total number of times this job was viewed.
     */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;


    /**
     * Total number of likes received by this job.
     */
    @Column(name = "like_count", nullable = false)
    private Long likeCount;


    // ============================================================
    // NEW — IMPORT TRACKING
    // ============================================================

    /**
     * Time when the job was imported/updated by the scraper.
     *
     * Used for:
     * Latest Jobs
     * Recent Imports
     */
    @Column(name = "imported_at")
    private LocalDateTime importedAt;


    // ============================================================
    // DEFAULT VALUES
    // ============================================================

   @PrePersist
public void prePersist() {

    if (createdAt == null) {
        createdAt = LocalDateTime.now();
    }

    if (postDate == null) {
        postDate = createdAt;
    }

    if (importedAt == null) {
        importedAt = createdAt;
    }

    if (viewCount == null) {
        viewCount = 0L;
    }

    if (likeCount == null) {
        likeCount = 0L;
    }

    if (isActive == null) {
        isActive = true;
    }
}


    // ============================================================
    // UPDATE IMPORT TIME
    // ============================================================

    @PreUpdate
    public void preUpdate() {

        if (viewCount == null) {
            viewCount = 0L;
        }

        if (likeCount == null) {
            likeCount = 0L;
        }

        if (importedAt == null) {
            importedAt = LocalDateTime.now();
        }
    }


}