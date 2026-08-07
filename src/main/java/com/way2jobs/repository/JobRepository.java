package com.way2jobs.repository;

import com.way2jobs.entity.Job;
import com.way2jobs.entity.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByState(State state);

    List<Job> findByStateId(Long stateId);

    List<Job> findByStateNameIgnoreCase(String stateName);

    Page<Job> findByStateNameIgnoreCase(String stateName, Pageable pageable);

    boolean existsByNotificationUrlIgnoreCase(String notificationUrl);

    boolean existsByApplyUrlIgnoreCase(String applyUrl);

    boolean existsByTitleIgnoreCaseAndLocationIgnoreCaseAndLastDateAndDepartmentIdAndCategoryIdAndStateId(
            String title,
            String location,
            java.time.LocalDate lastDate,
            Long departmentId,
            Long categoryId,
            Long stateId);

    Page<Job> findAll(Pageable pageable);

    Page<Job> findByStateId(Long stateId, Pageable pageable);
}