package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
}
