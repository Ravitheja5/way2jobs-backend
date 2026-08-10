package com.way2jobs.scraper.model;

import lombok.Data;

@Data
public class ScrapedJob {

    private String title;

    private String organizationName;

    private String postName;

    private String qualification;

    private String vacanciesRaw;

    private String salary;

    private String location;

    private String lastDateRaw;

    private String notificationUrl;

    private String applyUrl;

    private String sourceStateName;

    private String sourceUrl;
}