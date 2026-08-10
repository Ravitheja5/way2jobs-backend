package com.way2jobs.scraper;

import com.way2jobs.entity.Category;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.State;
import com.way2jobs.notification.entity.Notification;
import com.way2jobs.notification.repository.NotificationRepository;
import com.way2jobs.repository.JobRepository;
import com.way2jobs.scraper.model.ScrapedJob;
import com.way2jobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ScraperPersistenceService {

    private final JobRepository jobs;
    private final JobService jobService;
    private final NotificationRepository notifications;

    public sealed interface PersistOutcome
            permits Imported, Duplicate, Failed {
    }

    public record Imported(Long jobId) implements PersistOutcome {
    }

    public record Duplicate() implements PersistOutcome {
    }

    public record Failed(String reason) implements PersistOutcome {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PersistOutcome persistOne(
            ScrapedJob s,
            Department department,
            Category category,
            State state
    ) {

        try {

            LocalDate date = null;

            if (s.getLastDateRaw() != null
                    && !s.getLastDateRaw().isBlank()) {
                date = LocalDate.parse(s.getLastDateRaw());
            }

            // Duplicate check using current Job entity fields
            if ((s.getNotificationUrl() != null
                    && !s.getNotificationUrl().isBlank()
                    && jobs.existsByPdfNotificationIgnoreCase(
                    s.getNotificationUrl()))

                   

                    || jobs.existsByTitleIgnoreCaseAndLocationIgnoreCaseAndLastDateAndCategoryIgnoreCaseAndStateIgnoreCase(
                    s.getTitle(),
                    s.getLocation(),
                    date,
                    category != null ? category.getName() : null,
                    state != null ? state.getName() : null
            )) {

                return new Duplicate();
            }

           Job job = Job.builder()
        .title(s.getTitle())
        .organization(s.getOrganizationName())
        .postName(
                s.getPostName() != null && !s.getPostName().isBlank()
                        ? s.getPostName()
                        : s.getTitle()
        )
        .qualification(s.getQualification())
        .vacancies(parseVacancies(s.getVacanciesRaw()))
        .salary(s.getSalary())
        .location(s.getLocation())
        .lastDate(date)
        .pdfNotification(s.getNotificationUrl())
        .applyLink(s.getApplyUrl())
        .officialWebsite(
                s.getSourceUrl() != null
                        ? s.getSourceUrl()
                        : null
        )
        .category(
                category != null
                        ? category.getName()
                        : null
        )
        .state(
                state != null
                        ? state.getName()
                        : null
        )
        .source(s.getSourceUrl())
        .build();

            Job savedJob = jobService.saveJob(job);

            notifications.save(
                    Notification.builder()
                            .title("New Job: " + savedJob.getTitle())
                            .body(savedJob.getTitle())
                            .job(savedJob)
                            .type("NEW_JOB")
                            .build()
            );

            return new Imported(savedJob.getId());

        } catch (DataIntegrityViolationException e) {

            String message = e.getMostSpecificCause() != null
                    ? e.getMostSpecificCause().getMessage()
                    : e.getMessage();

            return new Failed(message);

        } catch (Exception e) {

            return new Failed(
                    e.getMessage() != null
                            ? e.getMessage()
                            : e.getClass().getSimpleName()
            );
        }
    }

    private Integer parseVacancies(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(
                    raw.replaceAll("[^0-9]", "")
            );
        } catch (Exception e) {
            return null;
        }
    }
}