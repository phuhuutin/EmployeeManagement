package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class AttendancePoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // One AttendancePoints can have multiple AttendanceRecords
    @OneToMany(mappedBy = "attendancePoints", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>(); // Initialize the list;


    public AttendancePoints(User user){
            this.user = user;
    }


    public int getPoints(){
        return attendanceRecords.stream()
                .mapToInt(record -> record.getReason().getPoints()) // Get points from each AttendanceRecord's reason
                .sum(); // Sum the points
    }

    public AttendanceRecord removeExpiredRecord(){
        if (!attendanceRecords.isEmpty()) {
            // Remove the last record and return it
            return this.attendanceRecords.remove(attendanceRecords.size() - 1);
        }
        return null; // Or throw an exception if you prefer
    }
    public void addNewRecord(AttendanceRecord newRecord){
        this.attendanceRecords.add(newRecord);
    }
}
