package com.way2jobs.scraper.source;

import com.way2jobs.scraper.config.ScraperProperties;
import com.way2jobs.scraper.model.ScrapedJob;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FreeJobAlertSource implements JobSource {

    private static final Pattern VACANCIES =
            Pattern.compile(
                    "(\\d{1,6})\\s*(?:posts?|vacanc(?:y|ies))",
                    Pattern.CASE_INSENSITIVE
            );

    @Override
    public String name() {
        return "FreeJobAlert";
    }

    @Override
    public List<ScrapedJob> fetch(
            String stateName,
            String url,
            ScraperProperties props
    ) throws Exception {

        Document document = Jsoup.connect(url)
                .userAgent(props.getUserAgent())
                .timeout(props.getTimeoutMs())
                .get();

        List<ScrapedJob> jobs = new ArrayList<>();

        boolean foundTable = false;

        for (Element table : document.select("table")) {

            Element header = table.selectFirst("tr");

            if (header == null) {
                continue;
            }

            Map<String, Integer> columns = detectColumns(header);

            /*
             * FreeJobAlert current table structure:
             *
             * Post Date
             * Job Title
             * Post name
             * Vacancies
             * Qualification
             * Last Date
             * Notification
             */

            if (!columns.containsKey("title")) {
                continue;
            }

            foundTable = true;

            for (Element row : table.select("tr")) {

                if (row.selectFirst("th") != null) {
                    continue;
                }

                try {

                    List<Element> cells =
                            row.select("> td");

                    if (cells.isEmpty()) {
                        continue;
                    }

                    ScrapedJob job =
                            parseRow(
                                    cells,
                                    columns,
                                    stateName,
                                    url
                            );

                    if (job != null) {
                        jobs.add(job);
                    }

                } catch (Exception e) {

                    log.warn(
                            "Unable to parse FreeJobAlert row from {}",
                            url,
                            e
                    );
                }
            }
        }

        if (!foundTable) {

            log.warn(
                    "No recognised FreeJobAlert job table found at {}",
                    url
            );
        }

        log.info(
                "FreeJobAlert source {} returned {} jobs",
                url,
                jobs.size()
        );

        return jobs;
    }

    /**
     * Detect columns from FreeJobAlert table header.
     */
    private Map<String, Integer> detectColumns(Element header) {

        Map<String, Integer> columns =
                new HashMap<>();

        List<Element> headers =
                header.select("> th, > td");

        for (int i = 0; i < headers.size(); i++) {

            String text =
                    normalize(headers.get(i).text());

            /*
             * Job Title
             */
            if ((text.equals("job title")
                    || text.contains("job title")
                    || text.equals("title"))
                    && !columns.containsKey("title")) {

                columns.put("title", i);
            }

            /*
             * Post Name
             */
            if ((text.equals("post name")
                    || text.contains("post name")
                    || text.equals("post"))
                    && !columns.containsKey("postName")) {

                columns.put("postName", i);
            }

            /*
             * Vacancies
             */
            if ((text.contains("vacanc")
                    || text.contains("no of post")
                    || text.contains("number of post"))
                    && !columns.containsKey("vacancies")) {

                columns.put("vacancies", i);
            }

            /*
             * Qualification
             */
            if ((text.contains("qualification")
                    || text.contains("eligibility"))
                    && !columns.containsKey("qualification")) {

                columns.put("qualification", i);
            }

            /*
             * Last Date
             */
            if ((text.contains("last date")
                    || text.contains("last dt")
                    || text.contains("closing"))
                    && !columns.containsKey("lastDate")) {

                columns.put("lastDate", i);
            }

            /*
             * Notification / Details
             */
            if ((text.contains("notification")
                    || text.contains("details")
                    || text.contains("apply")
                    || text.contains("more info"))
                    && !columns.containsKey("link")) {

                columns.put("link", i);
            }
        }

        return columns;
    }

    /**
     * Convert one HTML table row into ScrapedJob.
     */
    private ScrapedJob parseRow(
            List<Element> cells,
            Map<String, Integer> columns,
            String stateName,
            String sourceUrl
    ) {

        Element titleCell =
                getCell(
                        cells,
                        columns.get("title")
                );

        if (titleCell == null) {
            return null;
        }

        String sourceTitle =
                clean(titleCell.text());

        if (sourceTitle == null
                || sourceTitle.isBlank()) {

            return null;
        }

        ScrapedJob job =
                new ScrapedJob();

        /*
         * -----------------------------------------
         * ORGANIZATION
         * -----------------------------------------
         *
         * Example:
         *
         * MANUU Vacancy 2026 - 2 Lecturer Posts
         *
         * Organization:
         * MANUU
         *
         * Another:
         *
         * AIIMS Mangalagiri Vacancy 2026 - ...
         *
         * Organization:
         * AIIMS Mangalagiri
         */
        String organization =
                extractOrganization(sourceTitle);

        job.setOrganizationName(
                organization
        );

        /*
         * -----------------------------------------
         * POST NAME
         * -----------------------------------------
         */
        String postName =
                text(
                        getCell(
                                cells,
                                columns.get("postName")
                        )
                );

        job.setPostName(postName);

        /*
         * -----------------------------------------
         * TITLE
         * -----------------------------------------
         *
         * Use clean application title:
         *
         * MANUU - Lecturer Recruitment 2026
         */
        job.setTitle(
                buildTitle(
                        organization,
                        postName,
                        sourceTitle
                )
        );

        /*
         * -----------------------------------------
         * QUALIFICATION
         * -----------------------------------------
         */
        job.setQualification(
                text(
                        getCell(
                                cells,
                                columns.get("qualification")
                        )
                )
        );

        /*
         * -----------------------------------------
         * VACANCIES
         * -----------------------------------------
         */
        String vacancies =
                text(
                        getCell(
                                cells,
                                columns.get("vacancies")
                        )
                );

        if (vacancies == null
                || vacancies.isBlank()) {

            Matcher matcher =
                    VACANCIES.matcher(sourceTitle);

            if (matcher.find()) {
                vacancies =
                        matcher.group(1);
            }
        }

        job.setVacanciesRaw(vacancies);

        /*
         * -----------------------------------------
         * LAST DATE
         * -----------------------------------------
         */
        job.setLastDateRaw(
                text(
                        getCell(
                                cells,
                                columns.get("lastDate")
                        )
                )
        );

        /*
         * -----------------------------------------
         * NOTIFICATION URL
         * -----------------------------------------
         */
        Element link =
                extractLink(
                        cells,
                        columns.get("link")
                );

        if (link != null) {

            String href =
                    link.absUrl("href");

            if (href != null
                    && !href.isBlank()) {

                job.setNotificationUrl(href);

                /*
                 * Currently FreeJobAlert gives
                 * the details/notification link.
                 *
                 * Validation will use this URL
                 * for both fields if required.
                 */
                job.setApplyUrl(href);
            }
        }

        /*
         * -----------------------------------------
         * SOURCE INFORMATION
         * -----------------------------------------
         */
        job.setSourceStateName(stateName);

        job.setSourceUrl(sourceUrl);

        return job;
    }

    /**
     * Extract organization from Job Title.
     *
     * Examples:
     *
     * MANUU Vacancy 2026 - 2 Lecturer Posts
     * -> MANUU
     *
     * AIIMS Mangalagiri Vacancy 2026 - 4 Posts
     * -> AIIMS Mangalagiri
     *
     * DSH Kadapa Vacancy 2026 - ...
     * -> DSH Kadapa
     */
    private String extractOrganization(
            String sourceTitle
    ) {

        if (sourceTitle == null
                || sourceTitle.isBlank()) {

            return "";
        }

        String title =
                sourceTitle.trim();

        /*
         * Main pattern:
         *
         * XYZ Vacancy 2026
         */
        Pattern pattern =
                Pattern.compile(
                        "^(.+?)\\s+Vacancy\\b",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(title);

        if (matcher.find()) {

            return clean(
                    matcher.group(1)
            );
        }

        /*
         * Fallback:
         *
         * XYZ Recruitment 2026
         */
        pattern =
                Pattern.compile(
                        "^(.+?)\\s+Recruitment\\b",
                        Pattern.CASE_INSENSITIVE
                );

        matcher =
                pattern.matcher(title);

        if (matcher.find()) {

            return clean(
                    matcher.group(1)
            );
        }

        /*
         * If nothing matches, use first part
         * before '-' as fallback.
         */
        int dash =
                title.indexOf('-');

        if (dash > 0) {

            return clean(
                    title.substring(
                            0,
                            dash
                    )
            );
        }

        return title;
    }

    /**
     * Build clean application title.
     */
    private String buildTitle(
            String organization,
            String postName,
            String sourceTitle
    ) {

        if (organization != null
                && !organization.isBlank()
                && postName != null
                && !postName.isBlank()) {

            /*
             * Extract year if available.
             */
            Matcher yearMatcher =
                    Pattern.compile(
                            "\\b(20\\d{2})\\b"
                    ).matcher(sourceTitle);

            String year = "";

            if (yearMatcher.find()) {
                year =
                        " " + yearMatcher.group(1);
            }

            return organization.trim()
                    + " - "
                    + postName.trim()
                    + " Recruitment"
                    + year;
        }

        /*
         * Fallback to original source title.
         */
        return sourceTitle;
    }

    /**
     * Find link inside specified cell.
     * If notification column has no link,
     * search all cells.
     */
    private Element extractLink(
            List<Element> cells,
            Integer index
    ) {

        if (index != null
                && index >= 0
                && index < cells.size()) {

            Element link =
                    cells.get(index)
                            .selectFirst("a[href]");

            if (link != null) {
                return link;
            }
        }

        /*
         * Fallback:
         * search entire row.
         */
        for (Element cell : cells) {

            Element link =
                    cell.selectFirst("a[href]");

            if (link != null) {
                return link;
            }
        }

        return null;
    }

    private Element getCell(
            List<Element> cells,
            Integer index
    ) {

        if (index == null) {
            return null;
        }

        if (index < 0
                || index >= cells.size()) {

            return null;
        }

        return cells.get(index);
    }

    private String text(Element element) {

        if (element == null) {
            return null;
        }

        String value =
                element.text();

        return clean(value);
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value
                        .replace('\u00A0', ' ')
                        .replaceAll("\\s+", " ")
                        .trim();

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private String normalize(String value) {

        String cleaned =
                clean(value);

        if (cleaned == null) {
            return "";
        }

        return cleaned
                .toLowerCase(Locale.ROOT);
    }
}