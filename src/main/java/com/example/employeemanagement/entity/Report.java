package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportType type; // PAY or ATTENDANCE

    private LocalDate reportDate;
    @Column(length = 1024)
    private String details; // JSON or description of the report

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToOne
    @JoinColumn(name = "clock_in_out_record_id")
    @JsonIgnore
    private ClockInOutRecord clockInOutRecord = null;

    @OneToOne
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord = null;

    @OneToOne
    @JoinColumn(name = "shift_id")
    private Shift shift = null;

    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    @JsonProperty("username")
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    @JsonProperty("shift")
    public ShiftData getShiftData() {
        return shift != null ? shift.toShiftData(this.user) : null;
    }

    @JsonProperty("clock")
    public ClockInOutRecordData getClock(){
        return  clockInOutRecord != null ? this.clockInOutRecord.toClockInOutRecordData() : null;
    }

    private boolean isCompleted = false;

    public enum ReportType {
        SHIFT, ATTENDANCE, CLOCK
    }
}

