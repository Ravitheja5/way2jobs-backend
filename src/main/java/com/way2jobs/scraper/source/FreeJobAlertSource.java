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
                .followRedirects(true)
                .get();

        List<ScrapedJob> result = new ArrayList<>();

        /*
         * =========================================================
         * CASE 1:
         * URL itself is a FreeJobAlert ARTICLE
         * =========================================================
         */

        if (isArticlePage(document)) {

            ScrapedJob job = parseArticle(
                    document,
                    stateName,
                    url
            );

            if (job != null) {
                result.add(job);
            }

            return result;
        }

        /*
         * =========================================================
         * CASE 2:
         * URL is FreeJobAlert listing page
         *
         * Examples:
         * /ap-government-jobs/
         * /telangana-government-jobs/
         * /latest-notifications/
         * =========================================================
         */

        Set<String> articleUrls = new LinkedHashSet<>();

        for (Element a : document.select("a[href]")) {

            String href = a.absUrl("href");

            if (href == null || href.isBlank()) {
                continue;
            }

            if (!isFreeJobAlertArticle(href)) {
                continue;
            }

            articleUrls.add(href);
        }

        log.info(
                "Found {} FreeJobAlert article links for {}",
                articleUrls.size(),
                stateName
        );

        /*
         * =========================================================
         * Visit each article
         * =========================================================
         */

        for (String articleUrl : articleUrls) {

            try {

                Document article = Jsoup.connect(articleUrl)
                        .userAgent(props.getUserAgent())
                        .timeout(props.getTimeoutMs())
                        .followRedirects(true)
                        .get();

                ScrapedJob job = parseArticle(
                        article,
                        stateName,
                        articleUrl
                );

                if (job != null) {
                    result.add(job);
                }

                if (props.getPoliteDelayMs() > 0) {
                    Thread.sleep(
                            Math.min(
                                    props.getPoliteDelayMs(),
                                    1000
                            )
                    );
                }

            } catch (Exception e) {

                log.warn(
                        "Unable to parse FreeJobAlert article {}",
                        articleUrl,
                        e
                );
            }
        }

        return result;
    }

    /*
     * =========================================================
     * CHECK ARTICLE PAGE
     * =========================================================
     */

    private boolean isArticlePage(Document document) {

        return document.selectFirst(
                "script[type=application/ld+json]"
        ) != null
                &&
                (
                        document.selectFirst(
                                "meta[property=og:type][content=article]"
                        ) != null
                                ||
                        document.selectFirst(
                                "div.entry-content"
                        ) != null
                );
    }

    /*
     * =========================================================
     * CHECK FREEJOBALERT ARTICLE URL
     * =========================================================
     */

    private boolean isFreeJobAlertArticle(String url) {

        return url.contains("freejobalert.com/articles/");
    }

    /*
     * =========================================================
     * PARSE ARTICLE
     * =========================================================
     */

    private ScrapedJob parseArticle(
            Document document,
            String stateName,
            String sourceUrl
    ) {

        try {

            ScrapedJob job = new ScrapedJob();

            /*
             * -----------------------------------------------------
             * TITLE
             * -----------------------------------------------------
             */

            String pageTitle = text(
                    document.selectFirst(
                            "meta[property=og:title]"
                    ),
                    "content"
            );

            if (blank(pageTitle)) {

                Element titleElement =
                        document.selectFirst("h1");

                if (titleElement != null) {
                    pageTitle = titleElement.text();
                }
            }

            job.setTitle(cleanTitle(pageTitle));

            /*
             * -----------------------------------------------------
             * MAIN DETAILS TABLE
             *
             * Company Name
             * Post Name
             * No of Posts
             * Qualification
             * Walk-in Date
             * Apply Mode
             * Job Type
             * -----------------------------------------------------
             */

            Map<String, String> details =
                    extractDetailsTable(document);

            /*
             * -----------------------------------------------------
             * ORGANIZATION
             * -----------------------------------------------------
             *
             * First try "Company Name".
             *
             * If Company Name is empty, extract organization
             * from title.
             *
             * Example:
             *
             * MANUU - Lecturer Recruitment 2026
             *                 ↓
             * MANUU
             *
             * APCOB - Apprentices Recruitment 2026
             *                 ↓
             * APCOB
             */

            String organization =
                    details.get("company name");

            if (blank(organization)) {
                organization =
                        extractOrganizationFromTitle(
                                job.getTitle()
                        );
            }

            job.setOrganizationName(organization);

            /*
             * -----------------------------------------------------
             * POST NAME
             * -----------------------------------------------------
             */

            job.setPostName(
                    details.get("post name")
            );

            /*
             * -----------------------------------------------------
             * QUALIFICATION
             * -----------------------------------------------------
             */

            job.setQualification(
                    details.get("qualification")
            );

            /*
             * -----------------------------------------------------
             * VACANCIES
             * -----------------------------------------------------
             */

            job.setVacanciesRaw(
                    details.get("no of posts")
            );

            if (blank(job.getVacanciesRaw())) {

                job.setVacanciesRaw(
                        extractVacanciesFromTitle(
                                job.getTitle()
                        )
                );
            }

            /*
             * -----------------------------------------------------
             * IMPORTANT DATES
             * -----------------------------------------------------
             */

            String walkInDate =
                    details.get("walk-in date");

            if (blank(walkInDate)) {

                walkInDate =
                        details.get("last date");
            }

            job.setLastDateRaw(walkInDate);

            /*
             * -----------------------------------------------------
             * LOCATION
             * -----------------------------------------------------
             */

            String location =
                    extractLocation(document);

            job.setLocation(location);

            /*
             * -----------------------------------------------------
             * OFFICIAL NOTIFICATION
             * -----------------------------------------------------
             */

            String notification =
                    extractImportantLink(
                            document,
                            "official notification"
                    );

            if (blank(notification)) {

                notification =
                        extractImportantLink(
                                document,
                                "notification"
                        );
            }

            job.setNotificationUrl(notification);

            /*
             * -----------------------------------------------------
             * APPLY LINK
             * -----------------------------------------------------
             */

            String apply =
                    extractApplyLink(document);

            /*
             * For walk-in jobs there may be NO online apply link.
             *
             * In that case use notification URL as fallback.
             */

            if (blank(apply)) {
                apply = notification;
            }

            job.setApplyUrl(apply);

            /*
             * -----------------------------------------------------
             * SOURCE
             * -----------------------------------------------------
             */

            job.setSourceStateName(stateName);
            job.setSourceUrl(sourceUrl);

            /*
             * -----------------------------------------------------
             * LOG
             * -----------------------------------------------------
             */

            log.info(
                    "Parsed FJA job: organization={}, postName={}, vacancies={}, date={}",
                    job.getOrganizationName(),
                    job.getPostName(),
                    job.getVacanciesRaw(),
                    job.getLastDateRaw()
            );

            /*
             * -----------------------------------------------------
             * BASIC VALIDATION
             * -----------------------------------------------------
             */

            if (blank(job.getTitle())
                    && blank(job.getPostName())) {

                return null;
            }

            /*
             * If title is empty use post name
             */

            if (blank(job.getTitle())) {
                job.setTitle(job.getPostName());
            }

            /*
             * If post name is empty use title
             */

            if (blank(job.getPostName())) {
                job.setPostName(job.getTitle());
            }

            /*
             * If organization is still empty,
             * keep it empty.
             */

            if (blank(job.getOrganizationName())) {
                job.setOrganizationName("");
            }

            return job;

        } catch (Exception e) {

            log.warn(
                    "Unable to parse FreeJobAlert article {}",
                    sourceUrl,
                    e
            );

            return null;
        }
    }

    /*
     * =========================================================
     * EXTRACT ORGANIZATION FROM TITLE
     * =========================================================
     */

    private String extractOrganizationFromTitle(String title) {

        if (blank(title)) {
            return "";
        }

        String value = title.trim();

        /*
         * Example:
         *
         * MANUU - Lecturer Recruitment 2026
         *        ↑
         * organization = MANUU
         */

        if (value.contains(" - ")) {

            String organization =
                    value.substring(
                            0,
                            value.indexOf(" - ")
                    ).trim();

            if (!organization.isBlank()) {
                return organization;
            }
        }

        return "";
    }

    /*
     * =========================================================
     * EXTRACT DETAILS TABLE
     * =========================================================
     */

    private Map<String, String> extractDetailsTable(
            Document document
    ) {

        Map<String, String> result =
                new LinkedHashMap<>();

        /*
         * FreeJobAlert article has tables where:
         *
         * <td><strong>Company Name</strong></td>
         * <td>MANUU</td>
         *
         * Therefore don't depend on CSS classes.
         */

        for (Element table : document.select("table")) {

            for (Element row : table.select("tr")) {

                List<Element> cells =
                        row.select("> td, > th");

                if (cells.size() < 2) {
                    continue;
                }

                String key =
                        cells.get(0)
                                .text()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                String value =
                        cells.get(1)
                                .text()
                                .trim();

                if (blank(key) || blank(value)) {
                    continue;
                }

                result.put(
                        normalizeKey(key),
                        value
                );
            }
        }

        return result;
    }

    /*
     * =========================================================
     * NORMALIZE TABLE KEY
     * =========================================================
     */

    private String normalizeKey(String key) {

        return key
                .replace(":", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * =========================================================
     * IMPORTANT LINK
     * =========================================================
     */

    private String extractImportantLink(
            Document document,
            String keyword
    ) {

        for (Element h2 : document.select("h2")) {

            String heading =
                    h2.text()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (!heading.contains("important links")
                    && !heading.contains("notification")) {
                continue;
            }

            Element next =
                    h2.nextElementSibling();

            int safety = 0;

            while (next != null && safety++ < 10) {

                for (Element a :
                        next.select("a[href]")) {

                    String text =
                            a.text()
                                    .trim()
                                    .toLowerCase(Locale.ROOT);

                    String href =
                            a.absUrl("href");

                    if (blank(href)) {
                        continue;
                    }

                    if (text.contains(keyword)
                            || href.toLowerCase(
                                    Locale.ROOT
                            ).contains(".pdf")) {

                        return href;
                    }
                }

                next = next.nextElementSibling();
            }
        }

        /*
         * Fallback: search entire document
         */

        for (Element a :
                document.select("a[href]")) {

            String text =
                    a.text()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            String href =
                    a.absUrl("href");

            if (blank(href)) {
                continue;
            }

            if (text.contains(keyword)
                    && (
                    href.endsWith(".pdf")
                            || href.contains("notification")
            )) {

                return href;
            }
        }

        return null;
    }

    /*
     * =========================================================
     * APPLY LINK
     * =========================================================
     */

    private String extractApplyLink(
            Document document
    ) {

        for (Element a :
                document.select("a[href]")) {

            String text =
                    a.text()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            String href =
                    a.absUrl("href");

            if (blank(href)) {
                continue;
            }

            if (text.contains("apply online")
                    || text.equals("apply")
                    || text.contains("apply now")
                    || text.contains("online application")) {

                return href;
            }
        }

        return null;
    }

    /*
     * =========================================================
     * LOCATION
     * =========================================================
     */

    private String extractLocation(
            Document document
    ) {

        /*
         * First try JobPosting JSON-LD.
         */

        Element jsonLd =
                document.selectFirst(
                        "script[type=application/ld+json]"
                );

        if (jsonLd != null) {

            String json =
                    jsonLd.html();

            Pattern regionPattern =
                    Pattern.compile(
                            "\"addressRegion\"\\s*:\\s*\"([^\"]+)\"",
                            Pattern.CASE_INSENSITIVE
                    );

            Matcher matcher =
                    regionPattern.matcher(json);

            if (matcher.find()) {

                String region =
                        matcher.group(1);

                Pattern localityPattern =
                        Pattern.compile(
                                "\"addressLocality\"\\s*:\\s*\"([^\"]+)\"",
                                Pattern.CASE_INSENSITIVE
                        );

                Matcher localityMatcher =
                        localityPattern.matcher(json);

                if (localityMatcher.find()) {

                    return localityMatcher.group(1)
                            + ", "
                            + region;
                }

                return region;
            }
        }

        return null;
    }

    /*
     * =========================================================
     * VACANCIES FROM TITLE
     * =========================================================
     */

    private String extractVacanciesFromTitle(
            String title
    ) {

        if (blank(title)) {
            return null;
        }

        Matcher matcher =
                VACANCIES.matcher(title);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /*
     * =========================================================
     * TITLE CLEANING
     * =========================================================
     */

    private String cleanTitle(String title) {

        if (blank(title)) {
            return "";
        }

        return title
                .replaceAll(
                        "\\s*-\\s*Walkin\\s*$",
                        ""
                )
                .replaceAll(
                        "\\s*-\\s*Walk-in\\s*$",
                        ""
                )
                .trim();
    }

    /*
     * =========================================================
     * TEXT HELPER
     * =========================================================
     */

    private String text(
            Element element,
            String attribute
    ) {

        if (element == null) {
            return null;
        }

        if (attribute != null) {

            String value =
                    element.attr(attribute);

            return blank(value)
                    ? null
                    : value.trim();
        }

        String value =
                element.text();

        return blank(value)
                ? null
                : value.trim();
    }

    /*
     * =========================================================
     * BLANK HELPER
     * =========================================================
     */

    private boolean blank(String value) {

        return value == null
                || value.isBlank();
    }
}