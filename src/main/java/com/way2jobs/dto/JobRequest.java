package com.way2jobs.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class JobRequest {

    private String title;

    private String qualification;

    private Integer vacancies;

    private String salary;

    private String location;

    private LocalDate lastDate;

    private String notificationUrl;

    private String applyUrl;

    private Long departmentId;

    private Long categoryId;

    private Long stateId;
}