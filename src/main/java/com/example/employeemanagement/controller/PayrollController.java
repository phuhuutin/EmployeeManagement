package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.Payroll;
import com.example.employeemanagement.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/payroll")
public class PayrollController {
    @Autowired
    private PayrollService payrollService;
    @GetMapping
    public ResponseEntity<?> getShiftsPostedInLatestWeek() {
        try {
            List<Payroll> payrolls = payrollService.weekPayEvaluation();
            return ResponseEntity.ok(payrolls);
        } catch (Exception e) {
            // Return error response with the exception message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing payroll: " + e.getMessage());
        }
    }
}
