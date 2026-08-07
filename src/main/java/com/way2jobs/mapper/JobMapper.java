package com.way2jobs.mapper;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.dto.JobDetailResponse;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.Job;

public class JobMapper {

    private static final String DEFAULT_DEPARTMENT_LOGO_URL = "/default-lion-logo.svg";

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String resolveLogoUrl(Department department) {
        if (department == null) {
            return DEFAULT_DEPARTMENT_LOGO_URL;
        }

        String logoPath = valueOrEmpty(department.getLogoPath());
        if (logoPath.isEmpty()) {
            return DEFAULT_DEPARTMENT_LOGO_URL;
        }

        return logoPath;
    }

    public static JobCardResponse toJobCard(Job job) {
        return toJobCard(job, false);
    }

    public static JobCardResponse toJobCard(Job job, boolean saved) {
        if (job == null) {
            return null;
        }

        Department department = job.getDepartment();

        return JobCardResponse.builder()
                .id(job.getId())
                .title(valueOrEmpty(job.getTitle()))
                .department(department != null ? valueOrEmpty(department.getName()) : "")
                .departmentLogo(resolveLogoUrl(department))
                .qualification(valueOrEmpty(job.getQualification()))
                .salary(valueOrEmpty(job.getSalary()))
                .location(valueOrEmpty(job.getLocation()))
                .lastDate(job.getLastDate())
                .applyUrl(valueOrEmpty(job.getApplyUrl()))
                .notificationUrl(valueOrEmpty(job.getNotificationUrl()))
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

        Department department = job.getDepartment();

        return JobDetailResponse.builder()
                .id(job.getId())
                .title(valueOrEmpty(job.getTitle()))
                .department(department != null ? valueOrEmpty(department.getName()) : "")
                .departmentLogo(resolveLogoUrl(department))
                .qualification(valueOrEmpty(job.getQualification()))
                .salary(valueOrEmpty(job.getSalary()))
                .location(valueOrEmpty(job.getLocation()))
                .lastDate(job.getLastDate())
                .notificationUrl(valueOrEmpty(job.getNotificationUrl()))
                .applyUrl(valueOrEmpty(job.getApplyUrl()))
                .saved(saved)
                .build();
    }
}
