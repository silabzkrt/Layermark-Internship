package com.hrmanproject.domain.employee.repository;

import com.hrmanproject.domain.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByMail(String mail);

    Optional<Employee> findByPhoneNum(String phoneNum);

    boolean existsByMail(String mail);

    boolean existsByPhoneNum(String phoneNum);
}
