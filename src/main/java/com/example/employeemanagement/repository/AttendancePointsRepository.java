package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.AttendancePoints;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendancePointsRepository extends JpaRepository<AttendancePoints, Long> {
    // Find AttendancePoints by User
    Optional<AttendancePoints> findAttendancePointsByUser(User user);
}