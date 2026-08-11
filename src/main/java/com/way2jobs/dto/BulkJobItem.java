package com.way2jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkJobItem {

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

    /*
     * These are SOURCE VALUES.
     *
     * They are NOT database IDs.
     */
    private String category;

    private String state;
}