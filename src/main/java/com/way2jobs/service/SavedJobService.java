package com.way2jobs.service;

import com.way2jobs.entity.Job;
import java.util.List;

public interface SavedJobService {
    
    void saveJob(String token, Long jobId);
    
    List<Job> getSavedJobs(String token);
    
    void deleteSavedJob(String token, Long jobId);
}
