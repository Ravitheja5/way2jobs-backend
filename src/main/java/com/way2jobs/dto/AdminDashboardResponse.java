package com.way2jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalJobs;

    private long totalDepartments;

    private long totalCategories;

    private long totalStates;

    private long todayJobs;

}