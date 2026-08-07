package com.way2jobs.repository;

import com.way2jobs.entity.SavedJob;
import com.way2jobs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    List<SavedJob> findByUser(User user);
    
    Optional<SavedJob> findByUserAndJobId(User user, Long jobId);
    
    boolean existsByUserAndJobId(User user, Long jobId);
    
    void deleteByUserAndJobId(User user, Long jobId);
}
