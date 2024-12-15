package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jobrunr.jobs.JobId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;  // The date of the shift
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int workerLimit = 0;  // Maximum number of employees who can pick the shift

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // Track how many employees have picked the shift
    @Column(nullable = false)

    private int currentWorkers = 0;

    // Employees who picked this shift
    @ManyToMany(mappedBy = "pickedShifts")
    private Set<User> employees = new HashSet<>();

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ClockInOutRecord> clockInOutRecords = new ArrayList<>();

    // Manager who posted the shift
    @ManyToOne
    @JoinColumn(name = "user_id")
     private User postedBy;
    @JsonIgnore
    private UUID jobId;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Shift otherUser = (Shift)obj;
        return this.getId().equals(otherUser.getId());
    }

    public ShiftData toShiftData(User user) {
        // Find the ClockInOutRecord for the given user
        Optional<ClockInOutRecord> userClockRecord = clockInOutRecords.stream()
                .filter(clock -> clock.getUser().equals(user))
                .findFirst();
        // Map the current Shift and userClockRecord to a ShiftData object
        ShiftData shiftData = new ShiftData();
        shiftData.setId(this.id);
        shiftData.setStartTime(this.startTime);
        shiftData.setEndTime(this.endTime);
        return shiftData;
    }


}
