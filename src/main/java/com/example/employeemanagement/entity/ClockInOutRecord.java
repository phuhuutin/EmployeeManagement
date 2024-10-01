package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ClockInOutRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift;
}