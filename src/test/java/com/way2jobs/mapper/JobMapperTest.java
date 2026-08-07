package com.way2jobs.mapper;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.State;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JobMapperTest {

    @Test
    void shouldMapMinimalJobCardDetailsAndDefaultLogo() {
        Department department = Department.builder()
                .name("Railway")
                .logoPath(null)
                .build();

        State state = State.builder()
                .name("Kerala")
                .build();

        Job job = Job.builder()
                .id(42L)
                .title("Software Engineer")
                .qualification("B.Tech")
                .salary("5 LPA")
                .location("Kochi")
                .lastDate(LocalDate.of(2026, 8, 31))
                .notificationUrl("https://example.com/notify")
                .applyUrl("https://example.com/apply")
                .department(department)
                .state(state)
                .build();

        JobCardResponse response = JobMapper.toJobCard(job, true);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
        assertThat(response.getDepartment()).isEqualTo("Railway");
        assertThat(response.getDepartmentLogo()).isEqualTo("/default-lion-logo.svg");
        assertThat(response.getQualification()).isEqualTo("B.Tech");
        assertThat(response.getSalary()).isEqualTo("5 LPA");
        assertThat(response.getLocation()).isEqualTo("Kochi");
        assertThat(response.getLastDate()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(response.getNotificationUrl()).isEqualTo("https://example.com/notify");
        assertThat(response.getApplyUrl()).isEqualTo("https://example.com/apply");
        assertThat(response.isSaved()).isTrue();
    }
}
