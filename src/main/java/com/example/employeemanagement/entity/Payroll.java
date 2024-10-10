package com.example.employeemanagement.entity;

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
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double totalHoursWorked;  // Total hours worked in a week
    private double totalPay;  // Total pay for the employee
    @CreationTimestamp
    private LocalDate date;  // The date of the shift
    private LocalDate startPayDate;  // The date of the shift
    private LocalDate endPayDate;  // The date of the shift


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private double payRate;  // Pay rate for the employee

    public void addtotalHoursWorked(double time){
        this.totalHoursWorked += time;
    }
    public void addtotalPay(double pay){
        this.totalPay += pay;
    }

    public Payroll(LocalDate startPayDate, LocalDate endPayDate){
        this.startPayDate = startPayDate;
        this.endPayDate = endPayDate;
    }
}