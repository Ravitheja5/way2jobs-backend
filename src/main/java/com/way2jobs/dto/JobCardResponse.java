package com.way2jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCardResponse {

    // ============================================================
    // BASIC
    // ============================================================

    private Long id;

    private String jobId;

    private String state;

    private String organization;

    private String postName;

    private Integer vacancies;


    // ============================================================
    // JOB DETAILS
    // ============================================================

    private String qualification;

    private String salary;

    private LocalDate lastDate;

    private String location;

    private String selectionProcess;

    private String ageLimit;

    private String applicationFee;

    private String experience;


    // ============================================================
    // LINKS
    // ============================================================

    private String applyLink;

    private String pdfNotification;

    private String officialWebsite;


    // ============================================================
    // CLASSIFICATION
    // ============================================================

    private LocalDateTime postDate;

    private String category;

    private String source;


    // ============================================================
    // STATUS
    // ============================================================

    private Boolean isActive;


    // ============================================================
    // USER
    // ============================================================

    private boolean saved;


    // ============================================================
    // USER ENGAGEMENT
    // ============================================================

    private Long viewCount;

    private Long likeCount;


    // ============================================================
    // IMPORT
    // ============================================================

    private LocalDateTime importedAt;


    // ============================================================
    // EXPIRY
    // ============================================================

    private Long daysLeft;


    // ============================================================
    // SOURCE DISPLAY
    // ============================================================

    private String sourceLabel;

    private String sourceUrl;
}