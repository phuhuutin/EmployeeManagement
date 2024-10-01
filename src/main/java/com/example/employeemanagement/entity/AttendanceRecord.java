package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;  // Date of the record

    @Enumerated(EnumType.STRING) // Store enum as a string in the database
    private AttendanceReason reason;  // Attendance reason with associated points


    @ManyToOne
    @JoinColumn(name = "attendance_points_id")
    private AttendancePoints attendancePoints;
}
