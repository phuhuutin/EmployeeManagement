package com.example.employeemanagement.redis;

import com.example.employeemanagement.dto.SingleUserShiftData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleUserShiftDataCache implements Serializable {
    private static final long serialVersionUID = 1L;  // Recommended for Serializable classes
    @Id
    private Long id;
    private String username;
    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ClockInOutRecordCache clock;
    private Long employerId;

    public SingleUserShiftData mapToSingleUserShiftData(EmployerCache employerCache) {
        return new SingleUserShiftData(id, username, date, startTime, endTime, clock, employerCache);
    }
}
