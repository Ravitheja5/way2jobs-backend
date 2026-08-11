package com.way2jobs.service.impl;

import com.way2jobs.entity.Department;
import com.way2jobs.repository.DepartmentRepository;
import com.way2jobs.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Optional<Department> getDepartmentByShortName(
            String shortName
    ) {

        if (shortName == null || shortName.isBlank()) {
            return Optional.empty();
        }

        return departmentRepository.findByShortName(
                shortName.trim()
        );
    }

    @Override
    public Optional<Department> getDepartmentByName(
            String name
    ) {

        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return departmentRepository.findByName(
                name.trim()
        );
    }

    @Override
    public Department updateDepartment(
            Long id,
            Department department
    ) {

        Department existingDepartment =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found with id: "
                                                + id
                                )
                        );

        existingDepartment.setName(
                department.getName()
        );

        existingDepartment.setShortName(
                department.getShortName()
        );

        existingDepartment.setLogoPath(
                department.getLogoPath()
        );

        existingDepartment.setOfficialWebsite(
                department.getOfficialWebsite()
        );

        return departmentRepository.save(
                existingDepartment
        );
    }

    @Override
    public void deleteDepartment(Long id) {

        if (!departmentRepository.existsById(id)) {

            throw new RuntimeException(
                    "Department not found with id: " + id
            );
        }

        departmentRepository.deleteById(id);
    }
}