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

        List<ScrapedJob> result = new ArrayList<>();

        boolean tableFound = false;

        for (Element table : document.select("table")) {

            Element header = table.selectFirst("tr:has(th)");

            if (header == null) {
                continue;
            }

            Map<String, Integer> columns =
                    detectColumns(header);

            /*
             * We need the real Exam / Post Name column.
             */
            if (!columns.containsKey("postName")) {
                continue;
            }

            tableFound = true;

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
                        result.add(job);
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

        if (!tableFound) {

            log.warn(
                    "No recognised FreeJobAlert table found at {}",
                    url
            );
        }

        log.info(
                "FreeJobAlert: {} jobs parsed from {}",
                result.size(),
                url
        );

        return result;
    }


    // ============================================================
    // DETECT TABLE COLUMNS
    // ============================================================

    private Map<String, Integer> detectColumns(
            Element header
    ) {

        Map<String, Integer> columns =
                new HashMap<>();

        List<Element> headers =
                header.select("> th, > td");

        for (int i = 0; i < headers.size(); i++) {

            String text =
                    headers.get(i)
                            .text()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            /*
             * IMPORTANT:
             *
             * DO NOT use:
             *
             * text.contains("post")
             *
             * because "Post Date" would incorrectly
             * become Post Name.
             */

            // ----------------------------------------------------
            // POST DATE
            // ----------------------------------------------------

            if (
                    text.equals("post date")
                            || text.contains("post date")
            ) {

                columns.putIfAbsent(
                        "postDate",
                        i
                );
            }


            // ----------------------------------------------------
            // RECRUITMENT BOARD / ORGANIZATION
            // ----------------------------------------------------

            if (
                    text.contains("recruitment board")
                            || text.equals("organization")
                            || text.equals("organisation")
                            || text.equals("department")
            ) {

                columns.putIfAbsent(
                        "organization",
                        i
                );
            }


            // ----------------------------------------------------
            // EXAM / POST NAME
            // ----------------------------------------------------

            if (
                    text.contains("exam / post name")
                            || text.contains("exam/post name")
                            || text.equals("post name")
                            || text.equals("job name")
                            || text.equals("exam name")
            ) {

                columns.putIfAbsent(
                        "postName",
                        i
                );
            }


            // ----------------------------------------------------
            // QUALIFICATION
            // ----------------------------------------------------

            if (
                    text.contains("qualification")
                            || text.contains("eligibility")
            ) {

                columns.putIfAbsent(
                        "qualification",
                        i
                );
            }


            // ----------------------------------------------------
            // LAST DATE
            // ----------------------------------------------------

            if (
                    text.contains("last date")
                            || text.contains("closing date")
                            || text.contains("last dt")
            ) {

                columns.putIfAbsent(
                        "lastDate",
                        i
                );
            }


            // ----------------------------------------------------
            // ADVERTISEMENT NUMBER
            // ----------------------------------------------------

            if (
                    text.contains("advt")
                            || text.contains("advertisement")
            ) {

                columns.putIfAbsent(
                        "advertisement",
                        i
                );
            }


            // ----------------------------------------------------
            // MORE INFORMATION / LINK
            // ----------------------------------------------------

            if (
                    text.contains("more information")
                            || text.contains("details")
                            || text.contains("notification")
                            || text.contains("apply")
            ) {

                columns.putIfAbsent(
                        "link",
                        i
                );
            }
        }

        return columns;
    }


    // ============================================================
    // PARSE ROW
    // ============================================================

    private ScrapedJob parseRow(
            List<Element> cells,
            Map<String, Integer> columns,
            String stateName,
            String sourceUrl
    ) {

        /*
         * REAL POST NAME
         *
         * Example:
         *
         * Customer Service Associate / Clerk
         * Staff Assistant and AM – 338 Posts
         * Lecturer
         */

        Element postNameCell =
                cell(
                        cells,
                        columns.get("postName")
                );

        if (postNameCell == null) {
            return null;
        }

        String postName =
                cleanText(
                        postNameCell.text()
                );

        if (postName == null || postName.isBlank()) {
            return null;
        }


        // --------------------------------------------------------
        // ORGANIZATION
        // --------------------------------------------------------

        String organization =
                cleanText(
                        text(
                                cell(
                                        cells,
                                        columns.get("organization")
                                )
                        )
                );


        /*
         * If Recruitment Board is unavailable,
         * use post name temporarily.
         */
        if (organization == null ||
                organization.isBlank()) {

            organization = postName;
        }


        // --------------------------------------------------------
        // CREATE JOB
        // --------------------------------------------------------

        ScrapedJob job =
                new ScrapedJob();


        /*
         * TITLE
         *
         * Example:
         *
         * APCOB - Staff Assistant and AM – 338 Posts
         */
        job.setTitle(
                organization
                        + " - "
                        + postName
        );


        /*
         * ACTUAL POST NAME
         */
        job.setPostName(
                postName
        );


        /*
         * ACTUAL ORGANIZATION
         */
        job.setOrganizationName(
                organization
        );


        // --------------------------------------------------------
        // QUALIFICATION
        // --------------------------------------------------------

        job.setQualification(
                text(
                        cell(
                                cells,
                                columns.get("qualification")
                        )
                )
        );


        // --------------------------------------------------------
        // LAST DATE
        // --------------------------------------------------------

        job.setLastDateRaw(
                text(
                        cell(
                                cells,
                                columns.get("lastDate")
                        )
                )
        );


        // --------------------------------------------------------
        // VACANCIES
        // --------------------------------------------------------

        String vacancies =
                extractVacancies(
                        postName
                );

        job.setVacanciesRaw(
                vacancies
        );


        // --------------------------------------------------------
        // LINK
        // --------------------------------------------------------

        Element linkElement = null;

        Element linkCell =
                cell(
                        cells,
                        columns.get("link")
                );

        if (linkCell != null) {

            linkElement =
                    linkCell.selectFirst(
                            "a[href]"
                    );
        }


        /*
         * If link column was not detected,
         * search entire row.
         */
        if (linkElement == null) {

            linkElement =
                    cells.stream()
                            .map(
                                    c -> c.selectFirst(
                                            "a[href]"
                                    )
                            )
                            .filter(
                                    Objects::nonNull
                            )
                            .findFirst()
                            .orElse(null);
        }


        if (linkElement != null) {

            String absoluteUrl =
                    linkElement.absUrl(
                            "href"
                    );

            job.setNotificationUrl(
                    absoluteUrl
            );

            job.setApplyUrl(
                    absoluteUrl
            );
        }


        // --------------------------------------------------------
        // STATE
        // --------------------------------------------------------

        job.setSourceStateName(
                stateName
        );


        // --------------------------------------------------------
        // SOURCE URL
        // --------------------------------------------------------

        job.setSourceUrl(
                sourceUrl
        );


        return job;
    }


    // ============================================================
    // EXTRACT VACANCIES
    // ============================================================

    private String extractVacancies(
            String text
    ) {

        if (text == null ||
                text.isBlank()) {

            return null;
        }

        Matcher matcher =
                VACANCIES.matcher(text);

        if (matcher.find()) {

            return matcher.group(1);
        }

        return null;
    }


    // ============================================================
    // GET CELL
    // ============================================================

    private Element cell(
            List<Element> cells,
            Integer index
    ) {

        if (
                index == null
                        || index < 0
                        || index >= cells.size()
        ) {

            return null;
        }

        return cells.get(index);
    }


    // ============================================================
    // GET TEXT
    // ============================================================

    private String text(
            Element element
    ) {

        if (element == null) {
            return null;
        }

        String value =
                element.text().trim();

        return value.isBlank()
                ? null
                : value;
    }


    // ============================================================
    // CLEAN TEXT
    // ============================================================

    private String cleanText(
            String value
    ) {

        if (value == null) {
            return null;
        }

        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}