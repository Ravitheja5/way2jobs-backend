package com.way2jobs.mapper;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.entity.Job;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobMapperTest {

    @Test
    void shouldMapJobCardDetails() {

        Job job = Job.builder()
                .id(42L)
                .jobId("JOB-42")
                .title("Software Engineer")
                .organization("Railway")
                .postName("Software Engineer")
                .vacancies(10)
                .qualification("B.Tech")
                .salary("5 LPA")
                .location("Kochi")
                .lastDate(LocalDate.of(2026, 8, 31))
                .applyLink("https://example.com/apply")
                .pdfNotification("https://example.com/notification.pdf")
                .officialWebsite("https://example.com")
                .postDate(LocalDateTime.of(2026, 8, 1, 10, 0))
                .category("IT Jobs")
                .state("Kerala")
                .selectionProcess("Written Test + Interview")
                .ageLimit("18-30 Years")
                .applicationFee("₹500")
                .experience("0-2 Years")
                .isActive(true)
                .source("Government")
                .build();

        JobCardResponse response = JobMapper.toJobCard(job, true);

        assertThat(response).isNotNull();

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getJobId()).isEqualTo("JOB-42");
        assertThat(response.getState()).isEqualTo("Kerala");
        assertThat(response.getOrganization()).isEqualTo("Railway");
        assertThat(response.getPostName()).isEqualTo("Software Engineer");
        assertThat(response.getVacancies()).isEqualTo(10);
        assertThat(response.getQualification()).isEqualTo("B.Tech");
        assertThat(response.getSalary()).isEqualTo("5 LPA");
        assertThat(response.getLastDate())
                .isEqualTo(LocalDate.of(2026, 8, 31));

        assertThat(response.getApplyLink())
                .isEqualTo("https://example.com/apply");

        assertThat(response.getPdfNotification())
                .isEqualTo("https://example.com/notification.pdf");

        assertThat(response.getOfficialWebsite())
                .isEqualTo("https://example.com");

        assertThat(response.getPostDate())
                .isEqualTo(LocalDateTime.of(2026, 8, 1, 10, 0));

        assertThat(response.getCategory())
                .isEqualTo("IT Jobs");

        assertThat(response.getLocation())
                .isEqualTo("Kochi");

        assertThat(response.getSelectionProcess())
                .isEqualTo("Written Test + Interview");

        assertThat(response.getAgeLimit())
                .isEqualTo("18-30 Years");

        assertThat(response.getApplicationFee())
                .isEqualTo("₹500");

        assertThat(response.getExperience())
                .isEqualTo("0-2 Years");

        assertThat(response.getIsActive())
                .isTrue();

        assertThat(response.getSource())
                .isEqualTo("Government");

        assertThat(response.isSaved())
                .isTrue();
    }

    @Test
    void shouldMapSavedAsFalse() {

        Job job = Job.builder()
                .id(100L)
                .title("Java Developer")
                .organization("ABC Company")
                .state("Andhra Pradesh")
                .build();

        JobCardResponse response = JobMapper.toJobCard(job, false);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
assertThat(response.getOrganization()).isEqualTo("ABC Company");        assertThat(response.isSaved()).isFalse();
    }
}