package com.way2jobs.service;

import com.way2jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface JobService {

    Job saveJob(Job job);

    List<Job> getAllJobs();

    Optional<Job> getJobById(Long id);

    Job updateJob(Long id, Job job);

    void deleteJob(Long id);

    List<Job> getJobsByState(String state);

    List<Job> getAllIndiaJobs();

    Page<Job> getJobs(Pageable pageable);

    Page<Job> getJobsByState(
            String state,
            Pageable pageable
    );

    Page<Job> getAllIndiaJobs(
            Pageable pageable
    );
}