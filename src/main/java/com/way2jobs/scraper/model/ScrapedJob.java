package com.way2jobs.scraper.model;
import lombok.Data;
@Data public class ScrapedJob { private String title, organizationName, qualification, vacanciesRaw, salary, location, lastDateRaw, notificationUrl, applyUrl, sourceStateName, sourceUrl; }
