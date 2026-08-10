package com.way2jobs.scraper;

import com.way2jobs.entity.Category;
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

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private volatile ScraperRunResult lastResult;

    public boolean isRunning() {
        return running.get();
    }

    public ScraperRunResult getLastResult() {
        return lastResult;
    }

    public ScraperRunResult run() {

        ScraperRunResult result = start();

        if (!props.isEnabled()) {

            result.addMessage("scraper disabled");

            return finish(result);
        }

        if (!running.compareAndSet(false, true)) {

            result.addMessage("scraper already running");

            return finish(result);
        }

        try {

            /*
             * Refresh database lookup caches.
             */
            lookups.refreshCaches();

            /*
             * Resolve default category.
             */
            Category category =
                    lookups
                            .resolveCategory(
                                    props.getDefaultCategoryName()
                            )
                            .orElse(null);

            if (category == null) {

                result.addMessage(
                        "Category '"
                                + props.getDefaultCategoryName()
                                + "' does not exist."
                );

                return finish(result);
            }

            /*
             * Process configured sources.
             */
            for (var entry : props.getSources().entrySet()) {

                String stateName =
                        entry.getKey();

                String sourceUrl =
                        entry.getValue();

                /*
                 * Resolve state.
                 */
                State state =
                        lookups
                                .resolveState(stateName)
                                .orElse(null);

                if (state == null) {

                    result.addMessage(
                            "state not found: "
                                    + stateName
                    );

                    continue;
                }

                /*
                 * Get FreeJobAlert source.
                 */
                JobSource source =
                        sources.stream()
                                .filter(s ->
                                        "FreeJobAlert"
                                                .equalsIgnoreCase(
                                                        s.name()
                                                )
                                )
                                .findFirst()
                                .orElse(null);

                if (source == null) {

                    result.addMessage(
                            "FreeJobAlert source not configured"
                    );

                    break;
                }

                try {

                    result.setPagesVisited(
                            result.getPagesVisited() + 1
                    );

                    /*
                     * Scrape jobs.
                     */
                    List<ScrapedJob> rows =
                            source.fetch(
                                    stateName,
                                    sourceUrl,
                                    props
                            );

                    result.setRowsFound(
                            result.getRowsFound()
                                    + rows.size()
                    );

                    if (rows.isEmpty()) {

                        log.warn(
                                "No rows found from {}",
                                sourceUrl
                        );
                    }

                    /*
                     * Process every scraped job.
                     */
                    for (ScrapedJob row : rows) {

                        if (result.getImported()
                                >= props.getMaxJobsPerRun()) {

                            break;
                        }

                        /*
                         * Validate scraped job.
                         */
                        ScraperValidationService.ValidationResult validationResult =
                                validation.validate(row);

                        if (validationResult.reason() != null) {

                            result.setSkipped(
                                    result.getSkipped() + 1
                            );

                            result.addMessage(
                                    validationResult.reason()
                            );

                            continue;
                        }

                        ScrapedJob validJob =
                                validationResult.job();

                        /*
                         * IMPORTANT:
                         *
                         * Department is OPTIONAL.
                         *
                         * We do NOT skip a job when the
                         * department is missing.
                         *
                         * Organization is still saved
                         * directly into the Job entity.
                         */
                        var department =
                                lookups
                                        .resolveDepartment(
                                                validJob
                                                        .getOrganizationName()
                                        )
                                        .orElse(null);

                        /*
                         * Persist job.
                         *
                         * department can be null.
                         */
                        ScraperPersistenceService.PersistOutcome outcome =
                                persistence.persistOne(
                                        validJob,
                                        department,
                                        category,
                                        state
                                );

                        if (outcome
                                instanceof ScraperPersistenceService.Imported) {

                            result.setImported(
                                    result.getImported() + 1
                            );

                        } else if (outcome
                                instanceof ScraperPersistenceService.Duplicate) {

                            result.setDuplicates(
                                    result.getDuplicates() + 1
                            );

                        } else if (outcome
                                instanceof ScraperPersistenceService.Failed failed) {

                            result.setFailed(
                                    result.getFailed() + 1
                            );

                            result.addMessage(
                                    failed.reason()
                            );
                        }
                    }

                    /*
                     * Polite delay between sources.
                     */
                    if (props.getPoliteDelayMs() > 0) {

                        Thread.sleep(
                                props.getPoliteDelayMs()
                        );
                    }

                } catch (Exception e) {

                    log.error(
                            "Source failed: {}",
                            sourceUrl,
                            e
                    );

                    result.setFailed(
                            result.getFailed() + 1
                    );

                    result.addMessage(
                            "source failed: "
                                    + e.getMessage()
                    );
                }
            }

            /*
             * Send summary notification.
             */
            if (props.isNotifyOnNewJobs()
                    && result.getImported() > 0) {

                try {

                    int count =
                            result.getImported();

                    fcm.ifAvailable(
                            firebase ->
                                    firebase.sendNotification(
                                            "New Government Jobs",
                                            count
                                                    + " new jobs added. Tap to view.",
                                            null,
                                            Map.of(
                                                    "type",
                                                    "SCRAPER_SUMMARY",

                                                    "count",
                                                    String.valueOf(
                                                            count
                                                    )
                                            )
                                    )
                    );

                } catch (Exception e) {

                    log.warn(
                            "Summary push failed",
                            e
                    );
                }
            }

            return finish(result);

        } finally {

            running.set(false);
        }
    }

    private ScraperRunResult start() {

        ScraperRunResult result =
                new ScraperRunResult();

        result.setStartedAt(
                Instant.now()
        );

        return result;
    }

    private ScraperRunResult finish(
            ScraperRunResult result
    ) {

        result.setFinishedAt(
                Instant.now()
        );

        result.setDurationMs(
                result.getFinishedAt()
                        .toEpochMilli()
                        -
                        result.getStartedAt()
                                .toEpochMilli()
        );

        lastResult = result;

        return result;
    }
}