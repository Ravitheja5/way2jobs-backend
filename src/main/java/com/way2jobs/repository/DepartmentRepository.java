package com.way2jobs.repository;

import com.way2jobs.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByShortName(String shortName);

    boolean existsByName(String name);

    boolean existsByShortName(String shortName);

    Optional<Department> findByName(String name);
}