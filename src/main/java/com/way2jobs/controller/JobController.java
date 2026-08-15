package com.way2jobs.controller;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.dto.JobDetailResponse;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.User;
import com.way2jobs.mapper.JobMapper;
import com.way2jobs.repository.SavedJobRepository;
import com.way2jobs.repository.UserRepository;
import com.way2jobs.security.JwtUtil;
import com.way2jobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    // =========================================================
    // CREATE JOB
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Job createJob(@RequestBody Job job) {

        return jobService.saveJob(job);
    }


    // =========================================================
    // GET ALL JOBS
    // =========================================================

    @GetMapping
    public ResponseEntity<Page<JobCardResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getJobs(pageable)
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET JOB BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailResponse> getJobById(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Job job = jobService.getJobById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        )
                );

        return ResponseEntity.ok(
                JobMapper.toJobDetail(
                        job,
                        isJobSaved(
                                job.getId(),
                                authorization
                        )
                )
        );
    }


    // =========================================================
    // UPDATE JOB
    // =========================================================

    @PutMapping("/{id}")
    public Job updateJob(
            @PathVariable Long id,
            @RequestBody Job job) {

        return jobService.updateJob(id, job);
    }


    // =========================================================
    // DELETE JOB
    // =========================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(
            @PathVariable Long id) {

        jobService.deleteJob(id);
    }


    // =========================================================
    // GET JOBS BY STATE
    // =========================================================

    @GetMapping("/state/{state}")
    public ResponseEntity<Page<JobCardResponse>> getJobsByState(
            @PathVariable String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getJobsByState(
                                state,
                                pageable
                        )
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET ALL INDIA JOBS
    // =========================================================

    @GetMapping("/all-india")
    public ResponseEntity<Page<JobCardResponse>> getAllIndiaJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getAllIndiaJobs(pageable)
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — MOST VIEWED JOBS
    // =========================================================
    //
    // GET:
    // /api/jobs/most-viewed?page=0&size=20
    //
    // =========================================================

    @GetMapping("/most-viewed")
    public ResponseEntity<Page<JobCardResponse>> getMostViewedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getMostViewedJobs(pageable)
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — LATEST JOBS
    // =========================================================
    //
    // Based on postDate.
    // Returns active jobs posted within the last 6 days.
    //
    // GET:
    // /api/jobs/latest?page=0&size=20
    //
    // =========================================================

    @GetMapping("/latest")
    public ResponseEntity<Page<JobCardResponse>> getLatestJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getLatestJobs(pageable)
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — EXPIRING SOON
    // =========================================================
    //
    // Jobs expiring within 6 days.
    //
    // GET:
    // /api/jobs/expiring-soon?page=0&size=20
    //
    // =========================================================

    @GetMapping("/expiring-soon")
    public ResponseEntity<Page<JobCardResponse>> getExpiringSoonJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<JobCardResponse> response =
                jobService.getExpiringSoonJobs(pageable)
                        .map(job ->
                                JobMapper.toJobCard(
                                        job,
                                        isJobSaved(
                                                job.getId(),
                                                authorization
                                        )
                                )
                        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — RECORD JOB VIEW
    // =========================================================
    //
    // POST:
    // /api/jobs/{id}/view
    //
    // =========================================================

    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(
            @PathVariable Long id) {

        Job job = jobService.incrementViewCount(id);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("jobId", job.getId());
        response.put("viewCount", job.getViewCount());

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — LIKE JOB
    // =========================================================
    //
    // POST:
    // /api/jobs/{id}/like
    //
    // NOTE:
    // Proper user-level duplicate like prevention will be added
    // in the dedicated JobLike implementation stage.
    //
    // =========================================================

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> likeJob(
            @PathVariable Long id) {

        Job job = jobService.incrementLikeCount(id);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("jobId", job.getId());
        response.put("liked", true);
        response.put("likeCount", job.getLikeCount());

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — UNLIKE JOB
    // =========================================================
    //
    // DELETE:
    // /api/jobs/{id}/like
    //
    // =========================================================

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> unlikeJob(
            @PathVariable Long id) {

        Job job = jobService.decrementLikeCount(id);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("jobId", job.getId());
        response.put("liked", false);
        response.put("likeCount", job.getLikeCount());

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // NEW — FILTER COUNTS
    // =========================================================
    //
    // GET:
    // /api/jobs/filter-counts
    //
    // =========================================================

    @GetMapping("/state-counts")
    public ResponseEntity<Map<String, Long>> getStateCounts() {

        Map<String, Long> response = new LinkedHashMap<>();

        for (Object[] row : jobService.getActiveJobCountsByState()) {

            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }

            String state = row[0].toString().trim();

            if (state.isBlank()) {
                continue;
            }

            Number count = (Number) row[1];
            response.put(state, count == null ? 0L : count.longValue());
        }

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // FILTER COUNTS
    // =========================================================

    @GetMapping("/filter-counts")
    public ResponseEntity<Map<String, Object>> getFilterCounts() {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "total",
                jobService.getTotalActiveJobs()
        );

        response.put(
                "expiringSoon",
                jobService.getExpiringSoonJobCount()
        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // CHECK WHETHER JOB IS SAVED
    // =========================================================

    private boolean isJobSaved(
            Long jobId,
            String authorization) {

        if (jobId == null ||
                authorization == null ||
                authorization.isBlank()) {

            return false;
        }

        String token =
                authorization.startsWith("Bearer ")
                        ? authorization.substring(7)
                        : authorization;

        try {

            String email =
                    jwtUtil.extractEmail(token);

            if (email == null ||
                    email.isBlank()) {

                return false;
            }

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);

            return user != null &&
                    savedJobRepository.existsByUserAndJobId(
                            user,
                            jobId
                    );

        } catch (RuntimeException ex) {

            return false;
        }
    }

 
}