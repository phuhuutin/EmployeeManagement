package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreationTimestamp
    private LocalDate date;  // Date of the record

    @Enumerated(EnumType.STRING) // Store enum as a string in the database
    private AttendanceReason reason;  // Attendance reason with associated points

    @ManyToOne
    @JoinColumn(name = "shift_id")
    @JsonIgnore
    private Shift shift;

    public AttendanceRecord(Shift shift, AttendanceReason reason){
        this.shift = shift;
        this.reason = reason;
    }

    @ManyToOne
    @JoinColumn(name = "attendance_points_id")
    @JsonIgnore
    private AttendancePoints attendancePoints;

    @JsonProperty("shift_id")
    public Long getShiftId() {
        return shift != null ? shift.getId() : null;
    }
}
