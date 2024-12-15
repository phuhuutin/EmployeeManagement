package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.AttendancePoints;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendancePointsRepository extends JpaRepository<AttendancePoints, Long> {
    // Find AttendancePoints by User
    Optional<AttendancePoints> findAttendancePointsByUser(User user);
    @Modifying
    @Query("DELETE FROM AttendancePoints ap WHERE ap.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}