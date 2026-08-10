package com.way2jobs.service.impl;

import com.way2jobs.entity.Job;
import com.way2jobs.repository.JobRepository;
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
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        )
                );

        existingJob.setJobId(job.getJobId());
        existingJob.setTitle(job.getTitle());
        existingJob.setOrganization(job.getOrganization());
        existingJob.setPostName(job.getPostName());
        existingJob.setVacancies(job.getVacancies());
        existingJob.setQualification(job.getQualification());
        existingJob.setSalary(job.getSalary());
        existingJob.setLocation(job.getLocation());
        existingJob.setLastDate(job.getLastDate());
        existingJob.setApplyLink(job.getApplyLink());
        existingJob.setPdfNotification(job.getPdfNotification());
        existingJob.setOfficialWebsite(job.getOfficialWebsite());
        existingJob.setCategory(job.getCategory());
        existingJob.setState(job.getState());
        existingJob.setSelectionProcess(job.getSelectionProcess());
        existingJob.setAgeLimit(job.getAgeLimit());
        existingJob.setApplicationFee(job.getApplicationFee());
        existingJob.setExperience(job.getExperience());
        existingJob.setIsActive(job.getIsActive());
        existingJob.setSource(job.getSource());

        return jobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new RuntimeException(
                    "Job not found with id: " + id
            );
        }

        jobRepository.deleteById(id);
    }

    /*
     * JobService interface method
     */
    @Override
    public List<Job> getJobsByState(String state) {

        return jobRepository.findByStateIgnoreCase(
                state,
                Pageable.unpaged()
        ).getContent();
    }

    /*
     * Pagination version
     *
     * Do NOT add @Override unless this method exists
     * in JobService interface.
     */
    public Page<Job> getJobsByState(
            String state,
            Pageable pageable
    ) {
        if (state == null || state.isBlank() || "All India".equalsIgnoreCase(state.trim())) {
            return getAllIndiaJobs(pageable);
        }

        String normalizedState = state.trim();
        
        // Use 'containing' for better matching (handles things like "Andhra Pradesh" matching "AndhraPradesh" or "AP" matching "AP Govt")
        return jobRepository.findByStateContainingIgnoreCase(
                normalizedState,
                pageable
        );
    }

    @Override
    public List<Job> getAllIndiaJobs() {

        return jobRepository.findByStateIgnoreCase(
                "All India",
                Pageable.unpaged()
        ).getContent();
    }

    @Override
    public Page<Job> getJobs(Pageable pageable) {

        return jobRepository.findAll(pageable);
    }

    /*
     * State ID method is intentionally NOT @Override.
     *
     * Your current Job entity stores state as String,
     * not State entity.
     */
    public Page<Job> getJobsByState(
            Long stateId,
            Pageable pageable
    ) {

        throw new UnsupportedOperationException(
                "State ID filtering is not supported. " +
                "Job entity currently stores state as String."
        );
    }

    @Override
    public Page<Job> getAllIndiaJobs(Pageable pageable) {

        return jobRepository.findByStateIgnoreCase(
                "All India",
                pageable
        );
    }
}