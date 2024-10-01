package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double payRate;  // Pay rate for the employee
    private double totalHoursWorked;  // Total hours worked in a week
    private double totalPay;  // Total pay for the employee

    private LocalDate date;  // The date of the shift

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}