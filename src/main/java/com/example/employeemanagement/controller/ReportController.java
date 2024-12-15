package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ReportDTO;
import com.example.employeemanagement.entity.Report;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.ReportRepository;
import com.example.employeemanagement.service.ReportService;
import com.example.employeemanagement.service.ShiftService;
import com.example.employeemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private  final ReportRepository reportRepository;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);


    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    public ResponseEntity<?> createReport(@RequestBody ReportDTO report) {
        try{
            Report re = reportService.createReport(report);
            return ResponseEntity.ok(re);
        }catch (DataIntegrityViolationException exception){
            return ResponseEntity.badRequest().body("You already create a report for this " + report.getType());
        }catch (Exception exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
        }

    }
    @PreAuthorize("hasAuthority('MANAGER')")  // Only allow users with 'MANAGER' authority
    @GetMapping()
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
    @GetMapping("/{id}")
    public Report getById(@PathVariable Long id){
        return reportRepository.findById(id).orElseThrow();
    }

    @PreAuthorize("hasAuthority('MANAGER')")  // Only allow users with 'MANAGER' authority
    @GetMapping("/closed/{id}")
    public ResponseEntity<?> closedTheReport(@PathVariable Long id){
        try{
            Report re = reportService.closedReport(id);
            return ResponseEntity.ok(re);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}