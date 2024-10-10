package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.ClockInOutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClockInOutRecordRepository extends JpaRepository<ClockInOutRecord, Long> {}