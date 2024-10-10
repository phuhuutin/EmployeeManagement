package com.example.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDTO {
    private LocalDate date;           // The date of the shift
    private LocalDateTime startTime;  // Start time of the shift
    private LocalDateTime endTime;    // End time of the shift
    private int workerLimit;          // Maximum number of employees who can pick the shift
    private Long postedById;          // ID of the manager who is posting the shift


}