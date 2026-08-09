package com.way2jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCardResponse {
    private Long id;
    private String title;
    private String department;
    private String departmentLogo;
    private String qualification;
    private String salary;
    private String location;
    private LocalDate lastDate;
    private String applyUrl;
    private String notificationUrl;
    private String stateName;
    private boolean saved;

    public String getState() {
        return stateName;
    }
}
