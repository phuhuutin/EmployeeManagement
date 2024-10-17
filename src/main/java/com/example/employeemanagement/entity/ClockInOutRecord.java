package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class ClockInOutRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     private LocalDateTime clockInTime;
     private LocalDateTime clockOutTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    @JsonIgnore
    private Shift shift;

    @JsonProperty("shift_id")
    public Long getShiftId() {
        return shift != null ? shift.getId() : null;
    }

    @JsonProperty("user_id")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    public double getMinuteWorked(){
        return java.time.Duration.between(this.getClockInTime(), this.getClockOutTime()).toMinutes();
    }
    @Override
    public String toString() {
        return "ClockInOutRecord{" +
                "id=" + id +
                ", clockInTime=" + (clockInTime != null ? clockInTime.toString() : "Not clocked in") +
                ", clockOutTime=" + (clockOutTime != null ? clockOutTime.toString() : "Not clocked out") +
                ", userId=" + getUserId() +
                ", shiftId=" + getShiftId() +
                ", minutesWorked=" + getMinuteWorked() +
                '}';
    }

}