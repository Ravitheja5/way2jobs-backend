package com.way2jobs.scraper;

import com.way2jobs.entity.Category;
import com.way2jobs.entity.Department;
import com.way2jobs.entity.State;
import com.way2jobs.scraper.config.ScraperProperties;
import com.way2jobs.scraper.dto.ScraperRunResult;
import com.way2jobs.scraper.model.ScrapedJob;
import com.way2jobs.scraper.source.JobSource;
import com.way2jobs.service.FirebaseMessagingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobScraperService {

    private final ScraperProperties props;
    private final ScraperLookupService lookups;
    private final ScraperValidationService validation;
    private final ScraperPersistenceService persistence;
    private final List<JobSource> sources;
    private final ObjectProvider<FirebaseMessagingService> fcm;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile ScraperRunResult lastResult;


    // =========================================================
    // STATUS
    // =========================================================

    public boolean isRunning() {
        return running.get();
    }

    public ScraperRunResult getLastResult() {
        return lastResult;
    }


    // =========================================================
    // RUN SCRAPER
    // =========================================================

    public ScraperRunResult run() {

        ScraperRunResult r = start();

        // -----------------------------------------------------
        // CHECK ENABLED
        // -----------------------------------------------------

        if (!props.isEnabled()) {

            r.addMessage("scraper disabled");

            return finish(r);
        }


        // -----------------------------------------------------
        // PREVENT TWO SCRAPER RUNS AT SAME TIME
        // -----------------------------------------------------

        if (!running.compareAndSet(false, true)) {

            r.addMessage("scraper already running");

            return finish(r);
        }


        try {

            // -------------------------------------------------
            // REFRESH DATABASE CACHES
            // -------------------------------------------------

            lookups.refreshCaches();


            // -------------------------------------------------
            // RESOLVE DEFAULT CATEGORY
            // -------------------------------------------------

            Category cat =
                    lookups
                            .resolveCategory(
                                    props.getDefaultCategoryName()
                            )
                            .orElse(null);


            if (cat == null) {

                r.addMessage(
                        "Category '"
                                + props.getDefaultCategoryName()
                                + "' does not exist. "
                                + "Create it first via POST /api/categories."
                );

                return finish(r);
            }


            // =================================================
            // PROCESS ALL CONFIGURED SOURCES
            // =================================================

            for (var entry : props.getSources().entrySet()) {


                // -------------------------------------------------
                // CONVERT CONFIG KEY TO DATABASE STATE NAME
                // -------------------------------------------------

                String sourceStateName =
                        switch (entry.getKey()) {

                            case "AllIndia" ->
                                    "All India";

                            case "AndhraPradesh" ->
                                    "Andhra Pradesh";

                            case "Telangana" ->
                                    "Telangana";

                            default ->
                                    entry.getKey();
                        };


                log.info(
                        "Processing scraper source: {} -> {}",
                        entry.getKey(),
                        sourceStateName
                );


                // -------------------------------------------------
                // FIND STATE IN DATABASE
                // -------------------------------------------------

                State state =
                        lookups
                                .resolveState(sourceStateName)
                                .orElse(null);


                if (state == null) {

                    r.addMessage(
                            "state not found: "
                                    + sourceStateName
                    );

                    continue;
                }


                // -------------------------------------------------
                // FIND JOB SOURCE
                // -------------------------------------------------

                JobSource source =
                        sources.stream()
                                .findFirst()
                                .orElse(null);


                if (source == null) {

                    r.addMessage(
                            "no job source configured"
                    );

                    break;
                }


                try {

                    // -------------------------------------------------
                    // PAGE VISITED
                    // -------------------------------------------------

                    r.setPagesVisited(
                            r.getPagesVisited() + 1
                    );


                    // -------------------------------------------------
                    // FETCH JOBS
                    // -------------------------------------------------

                    List<ScrapedJob> rows =
                            source.fetch(
                                    sourceStateName,
                                    entry.getValue(),
                                    props
                            );


                    r.setRowsFound(
                            r.getRowsFound()
                                    + rows.size()
                    );


                    if (rows.isEmpty()) {

                        log.warn(
                                "No rows found for source {}",
                                entry.getValue()
                        );
                    }


                    // =================================================
                    // PROCESS EACH SCRAPED JOB
                    // =================================================

                    for (ScrapedJob row : rows) {


                        // -------------------------------------------------
                        // MAX JOB LIMIT
                        // -------------------------------------------------

                        if (
                                r.getImported()
                                        >= props.getMaxJobsPerRun()
                        ) {

                            break;
                        }


                        // -------------------------------------------------
                        // VALIDATE SCRAPED JOB
                        // -------------------------------------------------

                        ScraperValidationService.ValidationResult v =
                                validation.validate(row);


                        if (v.reason() != null) {

                            r.setSkipped(
                                    r.getSkipped() + 1
                            );

                            r.addMessage(
                                    v.reason()
                            );

                            continue;
                        }


                        // -------------------------------------------------
                        // RESOLVE DEPARTMENT
                        // -------------------------------------------------

                        String organization =
                                v.job()
                                        .getOrganizationName();


                        Department d =
                                lookups
                                        .resolveDepartment(
                                                organization
                                        )
                                        .orElse(null);


                        if (d == null) {

                            r.setSkipped(
                                    r.getSkipped() + 1
                            );

                            r.addMessage(
                                    "department not found: "
                                            + organization
                            );

                            continue;
                        }


                        // -------------------------------------------------
                        // SAVE JOB
                        // -------------------------------------------------

                        ScraperPersistenceService.PersistOutcome outcome =
                                persistence.persistOne(
                                        v.job(),
                                        d,
                                        cat,
                                        state
                                );


                        // -------------------------------------------------
                        // IMPORTED
                        // -------------------------------------------------

                        if (
                                outcome
                                        instanceof ScraperPersistenceService.Imported
                        ) {

                            r.setImported(
                                    r.getImported() + 1
                            );

                        }


                        // -------------------------------------------------
                        // DUPLICATE
                        // -------------------------------------------------

                        else if (
                                outcome
                                        instanceof ScraperPersistenceService.Duplicate
                        ) {

                            r.setDuplicates(
                                    r.getDuplicates() + 1
                            );

                        }


                        // -------------------------------------------------
                        // FAILED
                        // -------------------------------------------------

                        else {

                            r.setFailed(
                                    r.getFailed() + 1
                            );

                            r.addMessage(
                                    (
                                            (ScraperPersistenceService.Failed)
                                                    outcome
                                    ).reason()
                            );
                        }
                    }


                    // -------------------------------------------------
                    // POLITE DELAY
                    // -------------------------------------------------

                    if (
                            props.getPoliteDelayMs() > 0
                    ) {

                        Thread.sleep(
                                props.getPoliteDelayMs()
                        );
                    }


                } catch (Exception e) {

                    r.setFailed(
                            r.getFailed() + 1
                    );

                    r.addMessage(
                            "source failed: "
                                    + e.getMessage()
                    );

                    log.error(
                            "Scraper source failed: {}",
                            entry.getValue(),
                            e
                    );
                }
            }


            // =================================================
            // SEND SUMMARY NOTIFICATION
            // =================================================

            if (
                    props.isNotifyOnNewJobs()
                            && r.getImported() > 0
            ) {

                try {

                    int count =
                            r.getImported();

                    fcm.ifAvailable(
                            f ->
                                    f.sendNotification(
                                            "New Government Jobs",
                                            count
                                                    + " new jobs added. Tap to view.",
                                            null,
                                            Map.of(
                                                    "type",
                                                    "SCRAPER_SUMMARY",

                                                    "count",
                                                    String.valueOf(count)
                                            )
                                    )
                    );

                } catch (Exception e) {

                    log.warn(
                            "summary push failed",
                            e
                    );
                }
            }


            return finish(r);


        } finally {

            running.set(false);
        }
    }


    // =========================================================
    // START
    // =========================================================

    private ScraperRunResult start() {

        ScraperRunResult r =
                new ScraperRunResult();

        r.setStartedAt(
                Instant.now()
        );

        return r;
    }


    // =========================================================
    // FINISH
    // =========================================================

    private ScraperRunResult finish(
            ScraperRunResult r
    ) {

        r.setFinishedAt(
                Instant.now()
        );

        r.setDurationMs(
                r.getFinishedAt()
                        .toEpochMilli()
                        -
                        r.getStartedAt()
                                .toEpochMilli()
        );

        lastResult = r;

        return r;
    }
}