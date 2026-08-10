package com.way2jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
    return id;
}

    @Column(name = "job_id")
    private String jobId;

    @Column(nullable = false)
    private String title;

    @Column(name = "organization")
    private String organization;

    @Column(name = "post_name")
    private String postName;

    @Column(name = "vacancies")
    private Integer vacancies;

    @Column(name = "qualification", length = 500)
    private String qualification;

    @Column(name = "salary", length = 500)
    private String salary;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "last_date")
    private LocalDate lastDate;

    @Column(name = "apply_link", columnDefinition = "TEXT")
    private String applyLink;

    @Column(name = "pdf_notification", columnDefinition = "TEXT")
    private String pdfNotification;

    @Column(name = "official_website", columnDefinition = "TEXT")
    private String officialWebsite;

    @Column(name = "post_date")
    private LocalDateTime postDate;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "selection_process", columnDefinition = "TEXT")
    private String selectionProcess;

    @Column(name = "age_limit", length = 100)
    private String ageLimit;

    @Column(name = "application_fee", length = 100)
    private String applicationFee;

    @Column(name = "experience", length = 255)
    private String experience;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (postDate == null) {
            postDate = createdAt;
        }

        if (isActive == null) {
            isActive = true;
        }
    }
}