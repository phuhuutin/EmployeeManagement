package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    // Custom query to find shifts posted in the latest week (Sunday to Saturday)
    @Query("SELECT s FROM Shift s WHERE s.date BETWEEN :startOfWeek AND :endOfWeek")
    List<Shift> findShiftsPostedInLatestWeek(LocalDate startOfWeek, LocalDate endOfWeek);
}