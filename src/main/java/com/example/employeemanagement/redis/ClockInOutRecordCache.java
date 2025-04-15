package com.example.employeemanagement.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockInOutRecordCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private Double minuteWorked;
    private Long shift_id;
    private Long user_id;

//    public ClockInOutRecordCache(Long id, LocalDateTime clockInTime, LocalDateTime clockOutTime, Double minuteWorked, Long shift_id, Long user_id) {
//        this.id = id;
//        this.clockInTime = clockInTime;
//        this.clockOutTime = clockOutTime;
//        this.minuteWorked = minuteWorked;
//        this.shift_id = shift_id;
//        this.user_id = user_id;
//    }
}