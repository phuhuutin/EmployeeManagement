package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreationTimestamp
    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int workerLimit;  // Maximum number of employees who can pick the shift

    // Track how many employees have picked the shift
    @Column(nullable = false)
    private int currentWorkers = 0;

    // Employees who picked this shift
    @ManyToMany(mappedBy = "pickedShifts")
    private List<User> employees = new ArrayList<>();

    @OneToMany(mappedBy = "shift")
    private List<ClockInOutRecord> clockInOutRecords = new ArrayList<>();

    // Manager who posted the shift
    @ManyToOne
    @JoinColumn(name = "user_id")
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

    public void addClockInRecord(ClockInOutRecord clock){
        this.clockInOutRecords.add(clock);
    }

    @Override
    public String toString() {
        return "Shift{" +
                "id=" + id +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", workerLimit=" + workerLimit +
                ", currentWorkers=" + currentWorkers +
                ", employees=" + (employees != null ? employees.size() : 0) +
                ", postedBy=" + (postedBy != null ? postedBy.getUsername() : "N/A") +
                '}';
    }




}
