package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class ClockInOutRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nullable
    private LocalDateTime clockInTime = null;
    @Nullable
    private LocalDateTime clockOutTime = null;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    @JsonIgnore
    private Shift shift;

    @OneToOne
    @Nullable
    @JoinColumn(name = "payroll_id")
    @JsonIgnore
    private Payroll payroll = null;

    @OneToMany(mappedBy = "clockInOutRecord")
    @Nullable
    @JsonIgnore
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    @JsonProperty("shift_id")
    public Long getShiftId() {
        return shift != null ? shift.getId() : null;
    }

    @JsonProperty("user_id")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    public double getMinuteWorked(){
        if(this.clockInTime != null && this.clockOutTime != null) {
            return java.time.Duration.between(this.getClockInTime(), this.getClockOutTime()).toMinutes();
        }
        else
            return 0;
    }


    @Override
    public String toString() {
        return "ClockInOutRecord{" +
                "id=" + id +
                ", clockInTime=" + (clockInTime != null ? clockInTime.toString() : "Not clocked in") +
                ", clockOutTime=" + (clockOutTime != null ? clockOutTime.toString() : "Not clocked out") +
                ", userId=" + getUserId() +
                ", shiftId=" + getShiftId() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClockInOutRecord otherClock = (ClockInOutRecord)obj;
        return this.getId().equals(otherClock.getId());
    }

    public ClockInOutRecordData toClockInOutRecordData(){
        ClockInOutRecordData clockData = new ClockInOutRecordData();
        clockData.setId(this.id);
        clockData.setClockInTime(this.clockInTime);
        clockData.setClockOutTime(this.clockOutTime);
        return clockData;
    }

}