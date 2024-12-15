package com.example.employeemanagement.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClockInOutRecordDTO {
    Long id;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
}
