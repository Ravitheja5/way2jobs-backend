package com.way2jobs.mapper;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.dto.JobDetailResponse;
import com.way2jobs.entity.Job;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class JobMapper {

    // ============================================================
    // HELPER
    // ============================================================

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }


    // ============================================================
    // DAYS LEFT
    // ============================================================

    private static Long calculateDaysLeft(LocalDate lastDate) {

        if (lastDate == null) {
            return null;
        }

        return ChronoUnit.DAYS.between(
                LocalDate.now(),
                lastDate
        );
    }


    // ============================================================
    // SOURCE LABEL
    // Priority:
    // 1. Official Website
    // 2. Apply Link
    // 3. Notification
    // ============================================================

    private static String calculateSourceLabel(Job job) {

        if (job == null) {
            return null;
        }

        if (!valueOrEmpty(job.getOfficialWebsite()).isBlank()) {
            return "Official Website";
        }

        if (!valueOrEmpty(job.getApplyLink()).isBlank()) {
            return "Apply";
        }

        if (!valueOrEmpty(job.getPdfNotification()).isBlank()) {
            return "Notification";
        }

        return null;
    }


    // ============================================================
    // SOURCE URL
    // Same priority as source label
    // ============================================================

    private static String calculateSourceUrl(Job job) {

        if (job == null) {
            return null;
        }

        if (!valueOrEmpty(job.getOfficialWebsite()).isBlank()) {
            return valueOrEmpty(job.getOfficialWebsite());
        }

        if (!valueOrEmpty(job.getApplyLink()).isBlank()) {
            return valueOrEmpty(job.getApplyLink());
        }

        if (!valueOrEmpty(job.getPdfNotification()).isBlank()) {
            return valueOrEmpty(job.getPdfNotification());
        }

        return null;
    }


    // ============================================================
    // JOB CARD
    // ============================================================

    public static JobCardResponse toJobCard(Job job) {

        return toJobCard(job, false);
    }


    public static JobCardResponse toJobCard(
            Job job,
            boolean saved) {

        if (job == null) {
            return null;
        }

        return JobCardResponse.builder()

                // ------------------------------------------------
                // BASIC
                // ------------------------------------------------

                .id(job.getId())

                .jobId(
                        valueOrEmpty(
                                job.getJobId()
                        )
                )

                .state(
                        valueOrEmpty(
                                job.getState()
                        )
                )

                .organization(
                        valueOrEmpty(
                                job.getOrganization()
                        )
                )

                .postName(
                        !valueOrEmpty(
                                job.getPostName()
                        ).isBlank()
                                ? valueOrEmpty(
                                        job.getPostName()
                                )
                                : valueOrEmpty(
                                        job.getTitle()
                                )
                )

                .vacancies(
                        job.getVacancies()
                )


                // ------------------------------------------------
                // JOB DETAILS
                // ------------------------------------------------

                .qualification(
                        valueOrEmpty(
                                job.getQualification()
                        )
                )

                .salary(
                        valueOrEmpty(
                                job.getSalary()
                        )
                )

                .lastDate(
                        job.getLastDate()
                )

                .location(
                        valueOrEmpty(
                                job.getLocation()
                        )
                )

                .selectionProcess(
                        valueOrEmpty(
                                job.getSelectionProcess()
                        )
                )

                .ageLimit(
                        valueOrEmpty(
                                job.getAgeLimit()
                        )
                )

                .applicationFee(
                        valueOrEmpty(
                                job.getApplicationFee()
                        )
                )

                .experience(
                        valueOrEmpty(
                                job.getExperience()
                        )
                )


                // ------------------------------------------------
                // LINKS
                // ------------------------------------------------

                .applyLink(
                        valueOrEmpty(
                                job.getApplyLink()
                        )
                )

                .pdfNotification(
                        valueOrEmpty(
                                job.getPdfNotification()
                        )
                )

                .officialWebsite(
                        valueOrEmpty(
                                job.getOfficialWebsite()
                        )
                )


                // ------------------------------------------------
                // CLASSIFICATION
                // ------------------------------------------------

                .postDate(
                        job.getPostDate()
                )

                .category(
                        valueOrEmpty(
                                job.getCategory()
                        )
                )

                .source(
                        valueOrEmpty(
                                job.getSource()
                        )
                )


                // ------------------------------------------------
                // STATUS
                // ------------------------------------------------

                .isActive(
                        job.getIsActive()
                )


                // ------------------------------------------------
                // USER
                // ------------------------------------------------

                .saved(
                        saved
                )


                // ------------------------------------------------
                // ENGAGEMENT
                // ------------------------------------------------

                .viewCount(
                        job.getViewCount()
                )

                .likeCount(
                        job.getLikeCount()
                )


                // ------------------------------------------------
                // IMPORT
                // ------------------------------------------------

                .importedAt(
                        job.getImportedAt()
                )


                // ------------------------------------------------
                // EXPIRY
                // ------------------------------------------------

                .daysLeft(
                        calculateDaysLeft(
                                job.getLastDate()
                        )
                )


                // ------------------------------------------------
                // SOURCE DISPLAY
                // ------------------------------------------------

                .sourceLabel(
                        calculateSourceLabel(
                                job
                        )
                )

                .sourceUrl(
                        calculateSourceUrl(
                                job
                        )
                )


                .build();
    }


    // ============================================================
    // JOB DETAIL
    // ============================================================

    public static JobDetailResponse toJobDetail(
            Job job) {

        return toJobDetail(
                job,
                false
        );
    }


    public static JobDetailResponse toJobDetail(
            Job job,
            boolean saved) {

        if (job == null) {
            return null;
        }

        return JobDetailResponse.builder()

                // ------------------------------------------------
                // BASIC
                // ------------------------------------------------

                .id(
                        job.getId()
                )

                .jobId(
                        valueOrEmpty(
                                job.getJobId()
                        )
                )

                .state(
                        valueOrEmpty(
                                job.getState()
                        )
                )

                .organization(
                        valueOrEmpty(
                                job.getOrganization()
                        )
                )

                .postName(
                        !valueOrEmpty(
                                job.getPostName()
                        ).isBlank()
                                ? valueOrEmpty(
                                        job.getPostName()
                                )
                                : valueOrEmpty(
                                        job.getTitle()
                                )
                )

                .vacancies(
                        job.getVacancies()
                )


                // ------------------------------------------------
                // JOB DETAILS
                // ------------------------------------------------

                .qualification(
                        valueOrEmpty(
                                job.getQualification()
                        )
                )

                .salary(
                        valueOrEmpty(
                                job.getSalary()
                        )
                )

                .lastDate(
                        job.getLastDate()
                )

                .location(
                        valueOrEmpty(
                                job.getLocation()
                        )
                )

                .selectionProcess(
                        valueOrEmpty(
                                job.getSelectionProcess()
                        )
                )

                .ageLimit(
                        valueOrEmpty(
                                job.getAgeLimit()
                        )
                )

                .applicationFee(
                        valueOrEmpty(
                                job.getApplicationFee()
                        )
                )

                .experience(
                        valueOrEmpty(
                                job.getExperience()
                        )
                )


                // ------------------------------------------------
                // LINKS
                // ------------------------------------------------

                .applyLink(
                        valueOrEmpty(
                                job.getApplyLink()
                        )
                )

                .pdfNotification(
                        valueOrEmpty(
                                job.getPdfNotification()
                        )
                )

                .officialWebsite(
                        valueOrEmpty(
                                job.getOfficialWebsite()
                        )
                )


                // ------------------------------------------------
                // CLASSIFICATION
                // ------------------------------------------------

                .postDate(
                        job.getPostDate()
                )

                .category(
                        valueOrEmpty(
                                job.getCategory()
                        )
                )

                .source(
                        valueOrEmpty(
                                job.getSource()
                        )
                )


                // ------------------------------------------------
                // STATUS
                // ------------------------------------------------

                .isActive(
                        job.getIsActive()
                )


                // ------------------------------------------------
                // USER
                // ------------------------------------------------

                .saved(
                        saved
                )


                // ------------------------------------------------
                // ENGAGEMENT
                // ------------------------------------------------

                .viewCount(
                        job.getViewCount()
                )

                .likeCount(
                        job.getLikeCount()
                )


                // ------------------------------------------------
                // IMPORT
                // ------------------------------------------------

                .importedAt(
                        job.getImportedAt()
                )


                // ------------------------------------------------
                // EXPIRY
                // ------------------------------------------------

                .daysLeft(
                        calculateDaysLeft(
                                job.getLastDate()
                        )
                )


                // ------------------------------------------------
                // SOURCE DISPLAY
                // ------------------------------------------------

                .sourceLabel(
                        calculateSourceLabel(
                                job
                        )
                )

                .sourceUrl(
                        calculateSourceUrl(
                                job
                        )


                )

                .build();
    }
}