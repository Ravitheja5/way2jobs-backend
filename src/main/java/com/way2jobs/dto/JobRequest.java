package com.way2jobs.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class JobRequest {

    private String title;

    private String organization;

    private String postName;

    private String qualification;

    private Integer vacancies;

    private String salary;

    private String location;

    private LocalDate lastDate;

    private LocalDate postDate;

    private String ageLimit;

    private String experience;

    private String applicationFee;

    private String selectionProcess;

    private String notificationUrl;

    private String applyUrl;

    private String officialWebsite;

    private Long departmentId;

    private Long categoryId;

    private Long stateId;
}