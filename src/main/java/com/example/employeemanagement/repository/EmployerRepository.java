package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.entity.Payroll;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findById(Long id);

}
