package com.way2jobs.controller;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.mapper.JobMapper;
import com.way2jobs.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    public ResponseEntity<String> saveJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId) {
        savedJobService.saveJob(token, jobId);
        return ResponseEntity.ok("Job Saved Successfully");
    }

    @GetMapping
    public ResponseEntity<List<JobCardResponse>> getSavedJobs(
            @RequestHeader("Authorization") String token) {
        List<JobCardResponse> response = savedJobService.getSavedJobs(token)
                .stream()
                .map(JobMapper::toJobCard)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteSavedJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId) {
        savedJobService.deleteSavedJob(token, jobId);
        return ResponseEntity.ok("Saved Job Removed Successfully");
    }
}
