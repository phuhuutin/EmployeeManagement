package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int workerLimit;  // Maximum number of employees who can pick the shift

    // Track how many employees have picked the shift
    @Column(nullable = false)
    private int currentWorkers = 0;

    // Employees who picked this shift
    @ManyToMany(mappedBy = "pickedShifts")
    private List<User> employees;

    // Manager who posted the shift
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User postedBy;

    // Check if the shift is full
    public boolean isShiftFull() {
        return currentWorkers >= workerLimit;
    }

    // Method to add an employee to the shift
    public boolean addEmployee(User employee) {
        if (isShiftFull()) {
            return false;  // Cannot add more employees
        }
        employees.add(employee);
        currentWorkers++;
        return true;
    }


}
