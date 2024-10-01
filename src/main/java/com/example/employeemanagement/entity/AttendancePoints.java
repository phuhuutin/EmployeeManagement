package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class AttendancePoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int points;  // Total points accumulated by the employee

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // One AttendancePoints can have multiple AttendanceRecords
    @OneToMany(mappedBy = "attendancePoints", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceRecord> attendanceRecords;
}
