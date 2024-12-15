package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ReportDTO {
    private Report.ReportType type;
    private String details;
    private Long clockInOutRecordId;
    private Long attendanceRecordId;
    private Long shiftId;
}