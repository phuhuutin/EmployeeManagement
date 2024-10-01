package com.example.employeemanagement.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    // Role can be "MANAGER" or "EMPLOYEE"
    private String role;

    // Employees can pick multiple shifts
    @ManyToMany
    @JoinTable(
            name = "user_shifts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    private List<Shift> pickedShifts;

    // Managers can post multiple shifts
    @OneToMany(mappedBy = "postedBy")
    private List<Shift> postedShifts;
}
