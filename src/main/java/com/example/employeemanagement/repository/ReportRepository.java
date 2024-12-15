package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Report;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByReportDateDesc();

    List<Report> findByUserAndType(User user, Report.ReportType type);

    List<Report> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT r FROM Report r " +
            "JOIN r.shift s " +
            "WHERE s.employer.id = :employerId")
    List<Report> findAllCompletedReportsByEmployerId(@Param("employerId") Long employerId);
}