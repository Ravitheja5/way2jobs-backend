package com.way2jobs.mapper;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.dto.JobDetailResponse;
import com.way2jobs.entity.Job;

public class JobMapper {

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static JobCardResponse toJobCard(Job job) {
        return toJobCard(job, false);
    }

    public static JobCardResponse toJobCard(Job job, boolean saved) {

        if (job == null) {
            return null;
        }

        return JobCardResponse.builder()
                .id(job.getId())
                .jobId(valueOrEmpty(job.getJobId()))
                .state(valueOrEmpty(job.getState()))
                .organization(valueOrEmpty(job.getOrganization()))
                .postName(valueOrEmpty(job.getPostName()))
                .vacancies(job.getVacancies())
                .qualification(valueOrEmpty(job.getQualification()))
                .salary(valueOrEmpty(job.getSalary()))
                .lastDate(job.getLastDate())
                .applyLink(valueOrEmpty(job.getApplyLink()))
                .pdfNotification(valueOrEmpty(job.getPdfNotification()))
                .officialWebsite(valueOrEmpty(job.getOfficialWebsite()))
                .postDate(job.getPostDate())
                .category(valueOrEmpty(job.getCategory()))
                .location(valueOrEmpty(job.getLocation()))
                .selectionProcess(valueOrEmpty(job.getSelectionProcess()))
                .ageLimit(valueOrEmpty(job.getAgeLimit()))
                .applicationFee(valueOrEmpty(job.getApplicationFee()))
                .experience(valueOrEmpty(job.getExperience()))
                .isActive(job.getIsActive())
                .source(valueOrEmpty(job.getSource()))
                .saved(saved)
                .build();
    }

    public static JobDetailResponse toJobDetail(Job job) {
        return toJobDetail(job, false);
    }

    public static JobDetailResponse toJobDetail(Job job, boolean saved) {

        if (job == null) {
            return null;
        }

        return JobDetailResponse.builder()
                .id(job.getId())
                .jobId(valueOrEmpty(job.getJobId()))
                .state(valueOrEmpty(job.getState()))
                .organization(valueOrEmpty(job.getOrganization()))
                .postName(valueOrEmpty(job.getPostName()))
                .vacancies(job.getVacancies())
                .qualification(valueOrEmpty(job.getQualification()))
                .salary(valueOrEmpty(job.getSalary()))
                .lastDate(job.getLastDate())
                .applyLink(valueOrEmpty(job.getApplyLink()))
                .pdfNotification(valueOrEmpty(job.getPdfNotification()))
                .officialWebsite(valueOrEmpty(job.getOfficialWebsite()))
                .postDate(job.getPostDate())
                .category(valueOrEmpty(job.getCategory()))
                .location(valueOrEmpty(job.getLocation()))
                .selectionProcess(valueOrEmpty(job.getSelectionProcess()))
                .ageLimit(valueOrEmpty(job.getAgeLimit()))
                .applicationFee(valueOrEmpty(job.getApplicationFee()))
                .experience(valueOrEmpty(job.getExperience()))
                .isActive(job.getIsActive())
                .source(valueOrEmpty(job.getSource()))
                .saved(saved)
                .build();
    }
}