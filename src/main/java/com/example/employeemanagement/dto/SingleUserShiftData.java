package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.redis.ClockInOutRecordCache;
import com.example.employeemanagement.redis.EmployerCache;
import com.example.employeemanagement.redis.SingleUserShiftDataCache;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleUserShiftData implements Serializable {
    private static final long serialVersionUID = 1L;  // Recommended for Serializable classes

    private Long id;
    private String username;
    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ClockInOutRecordCache clock;
    private EmployerCache employer;

    public SingleUserShiftDataCache mapToSingleUserShiftDataCache() {
        return new SingleUserShiftDataCache(id, username, date, startTime, endTime, clock, employer.getId());
    }
}
