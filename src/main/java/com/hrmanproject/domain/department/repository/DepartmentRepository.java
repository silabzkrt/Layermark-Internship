package com.hrmanproject.domain.department.repository;

import com.hrmanproject.domain.department.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(Long id);

    List<Department> findAll();

    void deleteById(Long id);

    Optional<Department> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    void deleteAll();

    long count();
}
