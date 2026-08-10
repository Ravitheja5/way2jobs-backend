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
            String state,
            String url,
            ScraperProperties props
    ) throws Exception {

        Document d = Jsoup.connect(url)
                .userAgent(props.getUserAgent())
                .timeout(props.getTimeoutMs())
                .get();

        List<ScrapedJob> out = new ArrayList<>();

        boolean found = false;


        for (Element table : d.select("table")) {

            Element header =
                    table.selectFirst("tr:has(th)");

            if (header == null) {
                continue;
            }


            Map<String, Integer> cols =
                    columns(header);


            if (!cols.containsKey("title")) {
                continue;
            }


            found = true;


            for (Element row : table.select("tr")) {

                if (row.selectFirst("th") != null) {
                    continue;
                }


                try {

                    List<Element> cells =
                            row.select("> td");


                    if (!cells.isEmpty()) {

                        ScrapedJob job =
                                parseRow(
                                        cells,
                                        cols,
                                        state,
                                        url
                                );


                        if (job != null) {
                            out.add(job);
                        }
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


        if (!found) {

            log.warn(
                    "No recognised result table found at {}",
                    url
            );
        }


        return out;
    }


    // =========================================================
    // FIND TABLE COLUMNS
    // =========================================================

    private Map<String, Integer> columns(
            Element header
    ) {

        Map<String, Integer> map =
                new HashMap<>();


        List<Element> headers =
                header.select("> th, > td");


        for (int i = 0; i < headers.size(); i++) {

            String text =
                    headers.get(i)
                            .text()
                            .toLowerCase(Locale.ROOT);


            if (
                    (text.contains("title")
                            || text.contains("post"))
                            && !map.containsKey("title")
            ) {

                map.put("title", i);
            }


            if (
                    (text.contains("qualification")
                            || text.contains("eligibility"))
                            && !map.containsKey("qualification")
            ) {

                map.put("qualification", i);
            }


            if (
                    (text.contains("last date")
                            || text.contains("last dt")
                            || text.contains("closing"))
                            && !map.containsKey("lastDate")
            ) {

                map.put("lastDate", i);
            }


            if (
                    (text.contains("apply")
                            || text.contains("notification")
                            || text.contains("details")
                            || text.contains("more info"))
                            && !map.containsKey("link")
            ) {

                map.put("link", i);
            }


            if (
                    (text.contains("vacancy")
                            || text.contains("vacancies"))
                            && !map.containsKey("vacancies")
            ) {

                map.put("vacancies", i);
            }
        }


        return map;
    }


    // =========================================================
    // PARSE ROW
    // =========================================================

    private ScrapedJob parseRow(
            List<Element> cells,
            Map<String, Integer> columns,
            String state,
            String source
    ) {

        Element titleElement =
                cell(
                        cells,
                        columns.get("title")
                );


        if (titleElement == null) {
            return null;
        }


        String title =
                titleElement.text().trim();


        if (title.isBlank()) {
            return null;
        }


        ScrapedJob job =
                new ScrapedJob();


        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        job.setTitle(title);


        // -----------------------------------------------------
        // ORGANIZATION
        // -----------------------------------------------------

        String organization =
                extractOrganizationFromTitle(title);


        job.setOrganizationName(
                organization
        );


        // -----------------------------------------------------
        // QUALIFICATION
        // -----------------------------------------------------

        job.setQualification(
                text(
                        cell(
                                cells,
                                columns.get("qualification")
                        )
                )
        );


        // -----------------------------------------------------
        // LAST DATE
        // -----------------------------------------------------

        job.setLastDateRaw(
                text(
                        cell(
                                cells,
                                columns.get("lastDate")
                        )
                )
        );


        // -----------------------------------------------------
        // VACANCIES
        // -----------------------------------------------------

        String vacancies =
                text(
                        cell(
                                cells,
                                columns.get("vacancies")
                        )
                );


        if (
                vacancies == null
                        || vacancies.isBlank()
        ) {

            Matcher matcher =
                    VACANCIES.matcher(title);


            if (matcher.find()) {

                vacancies =
                        matcher.group(1);
            }
        }


        job.setVacanciesRaw(
                vacancies
        );


        // -----------------------------------------------------
        // LINK
        // -----------------------------------------------------

        Element link =
                Optional.ofNullable(
                        cell(
                                cells,
                                columns.get("link")
                        )
                )
                .map(
                        e -> e.selectFirst(
                                "a[href]"
                        )
                )
                .orElse(null);


        // fallback: find first link
        if (link == null) {

            link =
                    cells.stream()
                            .map(
                                    e -> e.selectFirst(
                                            "a[href]"
                                    )
                            )
                            .filter(
                                    Objects::nonNull
                            )
                            .findFirst()
                            .orElse(null);
        }


        if (link != null) {

            String href =
                    link.absUrl("href");


            if (
                    href != null
                            && !href.isBlank()
            ) {

                job.setNotificationUrl(
                        href
                );

                job.setApplyUrl(
                        href
                );
            }
        }


        // -----------------------------------------------------
        // SOURCE INFORMATION
        // -----------------------------------------------------

        job.setSourceStateName(
                state
        );

        job.setSourceUrl(
                source
        );


        log.info(
                "Scraped job: title='{}', organization='{}'",
                job.getTitle(),
                job.getOrganizationName()
        );


        return job;
    }


    // =========================================================
    // ORGANIZATION EXTRACTION
    // =========================================================

    private String extractOrganizationFromTitle(
            String title
    ) {

        if (
                title == null
                        || title.isBlank()
        ) {

            return "";
        }


        String value =
                title.trim();


        // -----------------------------------------------------
        // Example:
        //
        // MANUU - Lecturer Recruitment 2026
        //
        // -> MANUU
        // -----------------------------------------------------

        if (value.contains(" - ")) {

            String firstPart =
                    value.substring(
                            0,
                            value.indexOf(" - ")
                    ).trim();


            if (!firstPart.isBlank()) {

                return firstPart;
            }
        }


        // -----------------------------------------------------
        // Example:
        //
        // AIIMS Mangalagiri Lecturer Recruitment
        //
        // -> AIIMS Mangalagiri
        // -----------------------------------------------------

        String lower =
                value.toLowerCase(
                        Locale.ROOT
                );


        List<String> knownOrganizations =
                List.of(
                        "MANUU",
                        "APCOB",
                        "AIIMS Mangalagiri",
                        "DSH Kadapa",
                        "ANGRAU",
                        "IREL",
                        "SVIMS Tirupati",
                        "SVIMS",
                        "Visakhapatnam Port Authority",
                        "Narasapuram Area Hospital"
                );


        for (
                String organization :
                knownOrganizations
        ) {

            if (
                    lower.startsWith(
                            organization.toLowerCase(
                                    Locale.ROOT
                            )
                    )
            ) {

                return organization;
            }
        }


        // -----------------------------------------------------
        // If organization cannot be extracted
        // -----------------------------------------------------

        return "";
    }


    // =========================================================
    // CELL
    // =========================================================

    private Element cell(
            List<Element> cells,
            Integer index
    ) {

        return index != null
                && index >= 0
                && index < cells.size()
                ? cells.get(index)
                : null;
    }


    // =========================================================
    // TEXT
    // =========================================================

    private String text(
            Element element
    ) {

        return element == null
                ? null
                : element.text().trim();
    }
}