package com.hrmanproject.domain.department;

import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DepartmentJdbcIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindDepartmentUsingJdbcTemplate() {
        Department dept = new Department("Engineering", "ENG-001");
        Department saved = departmentRepository.save(dept);

        assertThat(saved.getId()).isNotNull();

        Optional<Department> foundById = departmentRepository.findById(saved.getId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getName()).isEqualTo("Engineering");
        assertThat(foundById.get().getCode()).isEqualTo("ENG-001");

        Optional<Department> foundByCode = departmentRepository.findByCode("ENG-001");
        assertThat(foundByCode).isPresent();

        assertThat(departmentRepository.existsByName("Engineering")).isTrue();
        assertThat(departmentRepository.existsByCode("ENG-001")).isTrue();

        List<Department> all = departmentRepository.findAll();
        assertThat(all).hasSize(1);
    }
}
