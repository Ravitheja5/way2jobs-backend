package com.way2jobs.scraper.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.way2jobs.scraper.config.ScraperProperties;
import com.way2jobs.scraper.model.ScrapedJob;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FreeJobAlertSource implements JobSource {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("(\\d{1,6})");

    private static final Pattern AGE_PATTERN =
            Pattern.compile(
                    "(?:minimum|min\\.?|from)\\s*(\\d{1,2})\\s*(?:years?|yrs?)?" +
                    ".*?(?:maximum|max\\.?|up to|upto)\\s*(\\d{1,2})\\s*(?:years?|yrs?)?",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

    private static final Pattern AGE_RANGE_PATTERN =
            Pattern.compile(
                    "(\\d{1,2})\\s*(?:to|-|–)\\s*(\\d{1,2})\\s*years?",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern EXPERIENCE_PATTERN =
            Pattern.compile(
                    "(?:minimum\\s+)?(?:experience|exp\\.?)\\s*(?:required)?\\s*[:\\-]?\\s*([^.;\\n]+)",
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

        Document listing = Jsoup.connect(url)
                .userAgent(props.getUserAgent())
                .timeout(props.getTimeoutMs())
                .followRedirects(true)
                .get();

        List<ScrapedJob> jobs = new ArrayList<>();

        /*
         * ============================================================
         * LISTING PAGE
         * ============================================================
         */

        for (Element table : listing.select("table")) {

            Element header = table.selectFirst("tr:has(th)");

            if (header == null) {
                continue;
            }

            Map<String, Integer> columns = columns(header);

            if (!columns.containsKey("title")) {
                continue;
            }

            for (Element row : table.select("tr")) {

                if (row.selectFirst("th") != null) {
                    continue;
                }

                List<Element> cells = row.select("> td");

                if (cells.isEmpty()) {
                    continue;
                }

                try {

                    ScrapedJob job =
                            parseListingRow(
                                    cells,
                                    columns,
                                    stateName,
                                    url
                            );

                    if (job == null) {
                        continue;
                    }

                    /*
                     * Detail page parsing
                     */
                    String detailUrl = firstValidUrl(
                            job.getNotificationUrl(),
                            job.getApplyUrl()
                    );

                    if (detailUrl != null &&
                            detailUrl.contains("freejobalert.com")) {

                        try {

                            enrichFromDetailPage(
                                    job,
                                    detailUrl,
                                    props
                            );

                        } catch (Exception e) {

                            log.warn(
                                    "Unable to enrich job detail: {}",
                                    detailUrl
                            );
                        }
                    }

                    jobs.add(job);

                } catch (Exception e) {

                    log.warn(
                            "Unable to parse FreeJobAlert row from {}",
                            url,
                            e
                    );
                }
            }
        }

        return jobs;
    }

    /*
     * ============================================================
     * TABLE COLUMN DETECTION
     * ============================================================
     */

    private Map<String, Integer> columns(Element header) {

        Map<String, Integer> map = new HashMap<>();

        List<Element> headers =
                header.select("> th, > td");

        for (int i = 0; i < headers.size(); i++) {

            String text =
                    normalize(headers.get(i).text());

            if (!map.containsKey("title") &&
                    (text.contains("title") ||
                     text.contains("post") ||
                     text.contains("job"))) {

                map.put("title", i);
            }

            if (!map.containsKey("qualification") &&
                    (text.contains("qualification") ||
                     text.contains("eligibility"))) {

                map.put("qualification", i);
            }

            if (!map.containsKey("lastDate") &&
                    (text.contains("last date") ||
                     text.contains("last dt") ||
                     text.contains("closing"))) {

                map.put("lastDate", i);
            }

            if (!map.containsKey("vacancies") &&
                    (text.contains("vacancy") ||
                     text.contains("vacancies") ||
                     text.contains("no of post") ||
                     text.contains("posts"))) {

                map.put("vacancies", i);
            }

            if (!map.containsKey("link") &&
                    (text.contains("apply") ||
                     text.contains("notification") ||
                     text.contains("details") ||
                     text.contains("more info"))) {

                map.put("link", i);
            }
        }

        return map;
    }

    /*
     * ============================================================
     * LISTING ROW
     * ============================================================
     */

    private ScrapedJob parseListingRow(
            List<Element> cells,
            Map<String, Integer> columns,
            String state,
            String source
    ) {

        Element titleCell =
                cell(cells, columns.get("title"));

        if (titleCell == null) {
            return null;
        }

        String title = clean(titleCell.text());

        if (title == null || title.isBlank()) {
            return null;
        }

        /*
         * Skip result / admit card / answer key / cutoff etc.
         */
        if (isNonJobNotification(title)) {
            return null;
        }

        ScrapedJob job = new ScrapedJob();

        job.setTitle(title);

        job.setPostName(extractPostName(title));

        job.setQualification(
                text(cell(cells, columns.get("qualification")))
        );

        job.setLastDateRaw(
                text(cell(cells, columns.get("lastDate")))
        );

        job.setVacanciesRaw(
                text(cell(cells, columns.get("vacancies")))
        );

        /*
         * Find link
         */
        Element link = null;

        Element linkCell =
                cell(cells, columns.get("link"));

        if (linkCell != null) {

            link = linkCell.selectFirst("a[href]");
        }

        if (link == null) {

            link = cells.stream()
                    .map(e -> e.selectFirst("a[href]"))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (link != null) {

            String href = link.absUrl("href");

            if (href != null && !href.isBlank()) {

                job.setNotificationUrl(href);
                job.setApplyUrl(href);
            }
        }

        job.setSourceStateName(state);
        job.setSourceUrl(source);

        return job;
    }

    /*
     * ============================================================
     * DETAIL PAGE ENRICHMENT
     * ============================================================
     */

    private void enrichFromDetailPage(
            ScrapedJob job,
            String detailUrl,
            ScraperProperties props
    ) throws Exception {

        Document detail =
                Jsoup.connect(detailUrl)
                        .userAgent(props.getUserAgent())
                        .timeout(props.getTimeoutMs())
                        .followRedirects(true)
                        .get();

        /*
         * ========================================================
         * JSON-LD JobPosting
         * ========================================================
         */

        for (Element script :
                detail.select("script[type=application/ld+json]")) {

            String json = script.html();

            if (json == null || json.isBlank()) {
                continue;
            }

            try {

                JsonNode root =
                        MAPPER.readTree(json);

                extractJsonLdJobPosting(
                        root,
                        job
                );

            } catch (Exception ignored) {
                // Some JSON-LD blocks are not valid JobPosting JSON.
            }
        }

        /*
         * ========================================================
         * ARTICLE TEXT
         * ========================================================
         */

        Element article =
                detail.selectFirst(".entry-content");

        String text;

        if (article != null) {
            text = article.text();
        } else {
            text = detail.body() != null
                    ? detail.body().text()
                    : "";
        }

        /*
         * Organization
         */
        if (blank(job.getOrganizationName())) {

            job.setOrganizationName(
                    extractOrganization(text, detail)
            );
        }

        /*
         * Post Name
         */
        if (blank(job.getPostName())) {

            String post =
                    extractPostNameFromText(text);

            if (!blank(post)) {
                job.setPostName(post);
            }
        }

        /*
         * Qualification
         */
        if (blank(job.getQualification())) {

            String qualification =
                    extractSection(
                            text,
                            "Educational Qualification",
                            "Qualification",
                            "Eligibility Criteria",
                            "Education Qualification"
                    );

            job.setQualification(
                    limit(qualification, 500)
            );
        }

        /*
         * Age
         */
        String age =
                extractAge(text);

        if (!blank(age)) {
            job.setAgeLimit(age);
        }

        /*
         * Experience
         */
        String experience =
                extractExperience(text);

        if (!blank(experience)) {
            job.setExperience(
                    limit(experience, 500)
            );
        }

        /*
         * Salary
         */
        String salary =
                extractSalary(text);

        if (!blank(salary)) {
            job.setSalary(
                    limit(salary, 300)
            );
        }

        /*
         * Location
         */
        if (blank(job.getLocation())) {

            String location =
                    extractLocation(text);

            if (!blank(location)) {
                job.setLocation(
                        limit(location, 300)
                );
            }
        }

        /*
         * Vacancy
         */
        if (blank(job.getVacanciesRaw())) {

            String vacancies =
                    extractVacancies(text);

            if (!blank(vacancies)) {
                job.setVacanciesRaw(vacancies);
            }
        }

        /*
         * Last date
         */
        if (blank(job.getLastDateRaw())) {

            String lastDate =
                    extractLastDate(text);

            if (!blank(lastDate)) {
                job.setLastDateRaw(lastDate);
            }
        }

        /*
         * Apply URL
         */
        String applyUrl =
                findApplyUrl(detail);

        if (!blank(applyUrl)) {
            job.setApplyUrl(applyUrl);
        }

        /*
         * Notification PDF
         */
        String notification =
                findNotificationUrl(detail);

        if (!blank(notification)) {
            job.setNotificationUrl(notification);
        }
    }

    /*
     * ============================================================
     * JSON-LD
     * ============================================================
     */

    private void extractJsonLdJobPosting(
            JsonNode root,
            ScrapedJob job
    ) {

        if (root == null) {
            return;
        }

        if (root.isArray()) {

            for (JsonNode node : root) {

                extractJsonLdJobPosting(
                        node,
                        job
                );
            }

            return;
        }

        JsonNode type =
                root.get("@type");

        if (type == null) {
            return;
        }

        boolean isJobPosting =
                "JobPosting".equalsIgnoreCase(
                        type.asText()
                );

        if (!isJobPosting) {
            return;
        }

        /*
         * Title
         */
        if (blank(job.getTitle()) &&
                root.has("title")) {

            job.setTitle(
                    clean(root.get("title").asText())
            );
        }

        /*
         * Organization
         */
        JsonNode organization =
                root.get("hiringOrganization");

        if (organization != null &&
                organization.has("name")) {

            job.setOrganizationName(
                    clean(
                            organization
                                    .get("name")
                                    .asText()
                    )
            );
        }

        /*
         * Location
         */
        JsonNode location =
                root.get("jobLocation");

        if (location != null) {

            String locationText =
                    extractJsonLocation(location);

            if (!blank(locationText)) {
                job.setLocation(locationText);
            }
        }

        /*
         * Salary
         */
        JsonNode salary =
                root.get("baseSalary");

        if (salary != null) {

            String salaryText =
                    extractJsonSalary(salary);

            if (!blank(salaryText)) {
                job.setSalary(salaryText);
            }
        }

        /*
         * Last date
         */
        if (root.has("validThrough")) {

            String validThrough =
                    root.get("validThrough").asText();

            if (!blank(validThrough)) {

                if (validThrough.length() >= 10) {
                    job.setLastDateRaw(
                            validThrough.substring(0, 10)
                    );
                }
            }
        }

        /*
         * Description
         */
        if (root.has("description")) {

            String description =
                    Jsoup.parse(
                            root.get("description").asText()
                    ).text();

            if (blank(job.getAgeLimit())) {

                String age =
                        extractAge(description);

                if (!blank(age)) {
                    job.setAgeLimit(age);
                }
            }

            if (blank(job.getExperience())) {

                String exp =
                        extractExperience(description);

                if (!blank(exp)) {
                    job.setExperience(exp);
                }
            }
        }
    }

    private String extractJsonLocation(
            JsonNode location
    ) {

        if (location.isArray()) {

            List<String> values =
                    new ArrayList<>();

            for (JsonNode node : location) {

                String value =
                        extractJsonLocation(node);

                if (!blank(value)) {
                    values.add(value);
                }
            }

            return String.join(", ", values);
        }

        JsonNode address =
                location.get("address");

        if (address != null) {

            List<String> parts =
                    new ArrayList<>();

            addJsonValue(
                    parts,
                    address,
                    "streetAddress"
            );

            addJsonValue(
                    parts,
                    address,
                    "addressLocality"
            );

            addJsonValue(
                    parts,
                    address,
                    "addressRegion"
            );

            addJsonValue(
                    parts,
                    address,
                    "postalCode"
            );

            return String.join(", ", parts);
        }

        return "";
    }

    private String extractJsonSalary(
            JsonNode salary
    ) {

        String currency =
                salary.has("currency")
                        ? salary.get("currency").asText()
                        : "INR";

        JsonNode value =
                salary.get("value");

        if (value == null) {
            return "";
        }

        if (value.isObject()) {

            String min =
                    value.has("minValue")
                            ? value.get("minValue").asText()
                            : null;

            String max =
                    value.has("maxValue")
                            ? value.get("maxValue").asText()
                            : null;

            String unit =
                    value.has("unitText")
                            ? value.get("unitText").asText()
                            : "";

            if (!blank(min) &&
                    !blank(max) &&
                    !min.equals(max)) {

                return currency + " " +
                        min + " - " +
                        max +
                        (blank(unit)
                                ? ""
                                : " / " + unit);
            }

            if (!blank(min)) {

                return currency + " " +
                        min +
                        (blank(unit)
                                ? ""
                                : " / " + unit);
            }
        }

        return "";
    }

    private void addJsonValue(
            List<String> parts,
            JsonNode node,
            String field
    ) {

        if (node.has(field)) {

            String value =
                    clean(node.get(field).asText());

            if (!blank(value)) {
                parts.add(value);
            }
        }
    }

    /*
     * ============================================================
     * FIELD EXTRACTION FROM ARTICLE
     * ============================================================
     */

    private String extractAge(String text) {

        if (blank(text)) {
            return null;
        }

        Matcher range =
                AGE_RANGE_PATTERN.matcher(text);

        while (range.find()) {

            String before =
                    text.substring(
                            Math.max(
                                    0,
                                    range.start() - 100
                            ),
                            range.end()
                    ).toLowerCase(Locale.ROOT);

            if (before.contains("age")) {

                return range.group(1) +
                        " to " +
                        range.group(2) +
                        " years";
            }
        }

        Matcher matcher =
                AGE_PATTERN.matcher(text);

        if (matcher.find()) {

            return matcher.group(1) +
                    " to " +
                    matcher.group(2) +
                    " years";
        }

        Pattern simple =
                Pattern.compile(
                        "age\\s*(?:limit)?\\s*[:\\-]?\\s*(\\d{1,2})\\s*(?:to|-|–)\\s*(\\d{1,2})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher simpleMatcher =
                simple.matcher(text);

        if (simpleMatcher.find()) {

            return simpleMatcher.group(1) +
                    " to " +
                    simpleMatcher.group(2) +
                    " years";
        }

        return null;
    }

    private String extractExperience(String text) {

        if (blank(text)) {
            return null;
        }

        Matcher matcher =
                EXPERIENCE_PATTERN.matcher(text);

        if (matcher.find()) {

            String value =
                    clean(matcher.group(1));

            if (!blank(value)) {
                return value;
            }
        }

        Pattern alternative =
                Pattern.compile(
                        "(\\d+\\+?\\s*(?:years?|yrs?)\\s*(?:of)?\\s*experience[^.]{0,200})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher m =
                alternative.matcher(text);

        if (m.find()) {
            return clean(m.group(1));
        }

        return null;
    }

    private String extractSalary(String text) {

        if (blank(text)) {
            return null;
        }

        Pattern salaryPattern =
                Pattern.compile(
                        "(?:₹|rs\\.?|inr)\\s*[\\d,]+(?:\\s*(?:-|to)\\s*(?:₹|rs\\.?|inr)?\\s*[\\d,]+)?(?:\\s*(?:per month|monthly|per annum|p\\.a\\.|per year|annually))?",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                salaryPattern.matcher(text);

        if (matcher.find()) {
            return clean(matcher.group());
        }

        return null;
    }

    private String extractLocation(String text) {

        if (blank(text)) {
            return null;
        }

        Pattern pattern =
                Pattern.compile(
                        "(?:job\\s*location|work\\s*location|place\\s*of\\s*posting|posting\\s*location)\\s*[:\\-]?\\s*([^.;\\n]{3,150})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {
            return clean(matcher.group(1));
        }

        return null;
    }

    private String extractVacancies(String text) {

        Pattern pattern =
                Pattern.compile(
                        "(?:total\\s+)?(?:number\\s+of\\s+)?(?:vacancies|posts|positions|seats)\\s*[:\\-]?\\s*(\\d{1,6})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        Pattern reverse =
                Pattern.compile(
                        "(\\d{1,6})\\s+(?:vacancies|posts|positions|seats)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher m =
                reverse.matcher(text);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private String extractLastDate(String text) {

        Pattern pattern =
                Pattern.compile(
                        "(?:last\\s+date|closing\\s+date|apply\\s+before)\\s*[:\\-]?\\s*(\\d{1,2}[./-]\\d{1,2}[./-]\\d{4})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractOrganization(
            String text,
            Document detail
    ) {

        Element title =
                detail.selectFirst("h1");

        if (title != null) {

            String value =
                    clean(title.text());

            if (value != null) {

                int index =
                        value.indexOf("Recruitment");

                if (index > 0) {

                    String prefix =
                            value.substring(0, index)
                                    .trim();

                    if (!prefix.isBlank()) {
                        return prefix;
                    }
                }
            }
        }

        return null;
    }

    private String extractPostNameFromText(
            String text
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?:post\\s+name|posts?|position)\\s*[:\\-]\\s*([^.;\\n]{3,120})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {
            return clean(matcher.group(1));
        }

        return null;
    }

    private String extractPostName(
            String title
    ) {

        if (blank(title)) {
            return null;
        }

        String value =
                title.replaceAll(
                        "(?i)\\s*recruitment\\s*202\\d.*$",
                        ""
                );

        value =
                value.replaceAll(
                        "(?i)\\s*-\\s*apply.*$",
                        ""
                );

        value =
                value.replaceAll(
                        "(?i)\\s*\\(\\d+\\s*posts?\\)",
                        ""
                );

        return clean(value);
    }

    private String extractSection(
            String text,
            String... headings
    ) {

        if (blank(text)) {
            return null;
        }

        for (String heading : headings) {

            Pattern pattern =
                    Pattern.compile(
                            Pattern.quote(heading) +
                            "\\s*[:\\-]?\\s*(.{20,500})",
                            Pattern.CASE_INSENSITIVE |
                            Pattern.DOTALL
                    );

            Matcher matcher =
                    pattern.matcher(text);

            if (matcher.find()) {

                return clean(
                        matcher.group(1)
                );
            }
        }

        return null;
    }

    /*
     * ============================================================
     * LINKS
     * ============================================================
     */

    private String findApplyUrl(
            Document document
    ) {

        for (Element a :
                document.select("a[href]")) {

            String text =
                    normalize(a.text());

            if (text.equals("apply online") ||
                    text.contains("apply online") ||
                    text.equals("apply now")) {

                return a.absUrl("href");
            }
        }

        return null;
    }

    private String findNotificationUrl(
            Document document
    ) {

        for (Element a :
                document.select("a[href]")) {

            String text =
                    normalize(a.text());

            String href =
                    a.absUrl("href");

            if (text.contains("notification") ||
                    text.contains("official notification") ||
                    (href != null &&
                     href.toLowerCase(Locale.ROOT)
                         .endsWith(".pdf"))) {

                return href;
            }
        }

        return null;
    }

    /*
     * ============================================================
     * NON-JOB FILTER
     * ============================================================
     */

    private boolean isNonJobNotification(
            String title
    ) {

        String value =
                normalize(title);

        return value.contains("result") ||
                value.contains("answer key") ||
                value.contains("cut off") ||
                value.contains("cutoff") ||
                value.contains("admit card") ||
                value.contains("hall ticket") ||
                value.contains("response sheet") ||
                value.contains("merit list") ||
                value.contains("provisional list") ||
                value.contains("exam date") ||
                value.contains("syllabus") ||
                value.contains("selection list");
    }

    /*
     * ============================================================
     * HELPERS
     * ============================================================
     */

    private Element cell(
            List<Element> cells,
            Integer index
    ) {

        if (index == null ||
                index < 0 ||
                index >= cells.size()) {

            return null;
        }

        return cells.get(index);
    }

    private String text(Element element) {

        return element == null
                ? null
                : clean(element.text());
    }

    private String firstValidUrl(
            String... urls
    ) {

        for (String url : urls) {

            if (!blank(url) &&
                    (url.startsWith("http://") ||
                     url.startsWith("https://"))) {

                return url;
            }
        }

        return null;
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.replace('\u00A0', ' ')
                        .replaceAll("\\s+", " ")
                        .trim();

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private String normalize(String value) {

        String cleaned =
                clean(value);

        return cleaned == null
                ? ""
                : cleaned.toLowerCase(
                        Locale.ROOT
                );
    }

    private boolean blank(String value) {

        return value == null ||
                value.isBlank();
    }

    private String limit(
            String value,
            int max
    ) {

        if (value == null) {
            return null;
        }

        return value.length() > max
                ? value.substring(0, max)
                : value;
    }
}