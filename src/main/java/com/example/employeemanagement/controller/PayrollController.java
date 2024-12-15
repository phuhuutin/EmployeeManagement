package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.Payroll;
import com.example.employeemanagement.service.AttendanceRecordService;
import com.example.employeemanagement.service.ManagerService;
import com.example.employeemanagement.service.PayrollService;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/payroll")
@AllArgsConstructor
public class PayrollController {
    private PayrollService payrollService;
    private ManagerService managerService;
    private AttendanceRecordService attendanceRecordService;
    @GetMapping("/{id}")
    private ResponseEntity<String> evaluateShiftById(@PathVariable Long id){
        try{
            AttendanceRecord record = attendanceRecordService.findById(id);
            attendanceRecordService.delete(record);
            BackgroundJob.delete(record.getJobId());
            return ResponseEntity.ok("OK");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
