package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Employer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleUserShiftData {
    private Long id;
    private String username;
    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ClockInOutRecord clock;
    private Employer employer;
}
