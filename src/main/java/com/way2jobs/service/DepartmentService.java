package com.way2jobs.service;

import com.way2jobs.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    Department saveDepartment(Department department);

    List<Department> getAllDepartments();

    Optional<Department> getDepartmentById(Long id);

    Optional<Department> getDepartmentByShortName(String shortName);

    Optional<Department> getDepartmentByName(String name);

    Department updateDepartment(Long id, Department department);

    void deleteDepartment(Long id);
}