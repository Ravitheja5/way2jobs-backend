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
public class JobDetailResponse {

    private Long id;

    private String jobId;

    private String state;

    private String organization;

    private String postName;

    private Integer vacancies;

    private String qualification;

    private String salary;

    private LocalDate lastDate;

    private String applyLink;

    private String pdfNotification;

    private String officialWebsite;

    private LocalDateTime postDate;

    private String category;

    private String location;

    private String selectionProcess;

    private String ageLimit;

    private String applicationFee;

    private String experience;

    private Boolean isActive;

    private String source;

    private boolean saved;
}