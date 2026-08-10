package com.way2jobs.scraper;

import com.way2jobs.scraper.config.ScraperProperties;
import com.way2jobs.scraper.model.ScrapedJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScraperValidationService {

    private final ScraperProperties props;

    public record ValidationResult(
            ScrapedJob job,
            String reason
    ) {
    }

    public ValidationResult validate(
            ScrapedJob job
    ) {

        /*
         * -----------------------------------------
         * BASIC TITLE VALIDATION
         * -----------------------------------------
         */

        if (job == null) {

            return new ValidationResult(
                    null,
                    "empty job"
            );
        }

        if (job.getTitle() == null
                || job.getTitle().trim().length() < 10) {

            return new ValidationResult(
                    null,
                    "title too short"
            );
        }

        job.setTitle(
                job.getTitle().trim()
        );

        /*
         * -----------------------------------------
         * SKIP NON-JOB NOTIFICATIONS
         * -----------------------------------------
         *
         * FreeJobAlert pages contain:
         *
         * Results
         * Hall Tickets
         * Answer Keys
         * Cut Off
         * Merit Lists
         *
         * These are NOT recruitment jobs.
         */

        if (isNonJobNotification(
                job.getTitle()
        )) {

            return new ValidationResult(
                    null,
                    "non-job notification"
            );
        }

        /*
         * -----------------------------------------
         * URL VALIDATION
         * -----------------------------------------
         */

        if (blank(
                job.getNotificationUrl()
        )) {

            job.setNotificationUrl(
                    job.getApplyUrl()
            );
        }

        if (blank(
                job.getApplyUrl()
        )) {

            job.setApplyUrl(
                    job.getNotificationUrl()
            );
        }

        if (blank(
                job.getNotificationUrl()
        )
                || blank(
                job.getApplyUrl()
        )) {

            return new ValidationResult(
                    null,
                    "no usable URL"
            );
        }

        if (!http(
                job.getNotificationUrl()
        )
                || !http(
                job.getApplyUrl()
        )) {

            return new ValidationResult(
                    null,
                    "invalid URL"
            );
        }

        /*
         * -----------------------------------------
         * LAST DATE
         * -----------------------------------------
         */

        LocalDate date =
                parseDate(
                        job.getLastDateRaw()
                );

        if (props.isSkipExpired()
                && date != null
                && date.isBefore(
                LocalDate.now()
        )) {

            return new ValidationResult(
                    null,
                    "expired"
            );
        }

        job.setLastDateRaw(
                date == null
                        ? null
                        : date.toString()
        );

        /*
         * -----------------------------------------
         * VACANCIES
         * -----------------------------------------
         */

        job.setVacanciesRaw(
                parseVacancies(
                        job.getVacanciesRaw()
                )
        );

        /*
         * -----------------------------------------
         * ORGANIZATION
         * -----------------------------------------
         */

        job.setOrganizationName(
                truncate(
                        clean(
                                job.getOrganizationName()
                        ),
                        255
                )
        );

        /*
         * -----------------------------------------
         * POST NAME
         * -----------------------------------------
         */

        job.setPostName(
                truncate(
                        clean(
                                job.getPostName()
                        ),
                        255
                )
        );

        /*
         * -----------------------------------------
         * OTHER FIELDS
         * -----------------------------------------
         */

        job.setQualification(
                truncate(
                        clean(
                                job.getQualification()
                        ),
                        255
                )
        );

        job.setSalary(
                truncate(
                        clean(
                                job.getSalary()
                        ),
                        255
                )
        );

        job.setLocation(
                truncate(
                        clean(
                                job.getLocation()
                        ),
                        255
                )
        );

        job.setTitle(
                truncate(
                        clean(
                                job.getTitle()
                        ),
                        255
                )
        );

        return new ValidationResult(
                job,
                null
        );
    }

    /*
     * =========================================
     * NON-JOB FILTER
     * =========================================
     */

    private boolean isNonJobNotification(
            String title
    ) {

        if (title == null
                || title.isBlank()) {

            return true;
        }

        String text =
                title
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .trim();

        return text.contains(" result ")
                || text.endsWith(" result")
                || text.contains(" results ")
                || text.endsWith(" results")

                || text.contains("admit card")
                || text.contains("hall ticket")

                || text.contains("answer key")
                || text.contains("answer keys")

                || text.contains("cut off")
                || text.contains("cutoff")
                || text.contains("cut-off")

                || text.contains("merit list")
                || text.contains("provisional list")
                || text.contains("selection list")

                || text.contains("response sheet")
                || text.contains("call letter")

                || text.contains("exam date")
                || text.contains("exam schedule");
    }

    /*
     * =========================================
     * DATE PARSER
     * =========================================
     */

    public LocalDate parseDate(
            String value
    ) {

        if (blank(value)) {
            return null;
        }

        String text =
                value.trim();

        List<String> patterns =
                List.of(
                        "dd-MM-yyyy",
                        "dd/MM/yyyy",
                        "dd.MM.yyyy",
                        "yyyy-MM-dd",
                        "dd MMM yyyy",
                        "dd MMMM yyyy",
                        "MMM dd, yyyy",
                        "dd-MMM-yyyy",
                        "dd/MMM/yyyy"
                );

        for (String pattern : patterns) {

            try {

                return LocalDate.parse(
                        text,
                        DateTimeFormatter.ofPattern(
                                pattern,
                                Locale.ENGLISH
                        )
                );

            } catch (
                    DateTimeParseException ignored
            ) {
            }
        }

        return null;
    }

    /*
     * =========================================
     * VACANCY PARSER
     * =========================================
     */

    private String parseVacancies(
            String value
    ) {

        if (blank(value)) {
            return null;
        }

        try {

            String numbers =
                    value.replaceAll(
                            "[^0-9]",
                            ""
                    );

            if (numbers.isBlank()) {
                return null;
            }

            return String.valueOf(
                    Integer.parseInt(numbers)
            );

        } catch (Exception e) {

            return null;
        }
    }

    /*
     * =========================================
     * HELPERS
     * =========================================
     */

    private boolean blank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    private boolean http(
            String value
    ) {

        return value != null
                && (
                value.startsWith(
                        "http://"
                )
                        || value.startsWith(
                        "https://"
                )
        );
    }

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value
                        .replace(
                                '\u00A0',
                                ' '
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private String truncate(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return null;
        }

        return value.length() > maxLength
                ? value.substring(
                0,
                maxLength
        )
                : value;
    }
}