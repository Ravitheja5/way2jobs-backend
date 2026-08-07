package com.way2jobs.service.impl;

import com.way2jobs.entity.Job;
import com.way2jobs.repository.JobRepository;
import com.way2jobs.repository.StateRepository;
import com.way2jobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final StateRepository stateRepository;

    @Override
    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    @Override
    public Job updateJob(Long id, Job job) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        existingJob.setTitle(job.getTitle());
        existingJob.setQualification(job.getQualification());
        existingJob.setVacancies(job.getVacancies());
        existingJob.setSalary(job.getSalary());
        existingJob.setLocation(job.getLocation());
        existingJob.setLastDate(job.getLastDate());
        existingJob.setNotificationUrl(job.getNotificationUrl());
        existingJob.setApplyUrl(job.getApplyUrl());
        existingJob.setDepartment(job.getDepartment());
        existingJob.setCategory(job.getCategory());
        existingJob.setState(job.getState());

        return jobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found with id: " + id);
        }

        jobRepository.deleteById(id);
    }

    @Override
    public List<Job> getJobsByState(Long stateId) {
        return jobRepository.findByStateId(stateId);
    }

    @Override
    public List<Job> getAllIndiaJobs() {
        return jobRepository.findByStateNameIgnoreCase("All India");
    }

    @Override
    public Page<Job> getJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    @Override
    public Page<Job> getJobsByState(Long stateId, Pageable pageable) {
        return jobRepository.findByStateId(stateId, pageable);
    }

    @Override
    public Page<Job> getAllIndiaJobs(Pageable pageable) {
        return jobRepository.findByStateNameIgnoreCase("All India", pageable);
    }
}