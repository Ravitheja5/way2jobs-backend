package com.way2jobs.service.impl;

import com.way2jobs.entity.Job;
import com.way2jobs.entity.SavedJob;
import com.way2jobs.entity.User;
import com.way2jobs.repository.JobRepository;
import com.way2jobs.repository.SavedJobRepository;
import com.way2jobs.repository.UserRepository;
import com.way2jobs.security.JwtUtil;
import com.way2jobs.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void saveJob(String token, Long jobId) {
        User user = getUserByToken(token);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (savedJobRepository.existsByUserAndJobId(user, jobId)) {
            throw new RuntimeException("Job already saved");
        }

        SavedJob savedJob = SavedJob.builder()
                .user(user)
                .job(job)
                .build();

        savedJobRepository.save(savedJob);
    }

    @Override
    public List<Job> getSavedJobs(String token) {
        User user = getUserByToken(token);
        List<SavedJob> savedJobs = savedJobRepository.findByUser(user);
        return savedJobs.stream()
                .map(SavedJob::getJob)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSavedJob(String token, Long jobId) {
        User user = getUserByToken(token);
        if (!savedJobRepository.existsByUserAndJobId(user, jobId)) {
            throw new RuntimeException("Saved job not found");
        }
        savedJobRepository.deleteByUserAndJobId(user, jobId);
    }

    private User getUserByToken(String token) {
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
