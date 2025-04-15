package com.example.employeemanagement.dto;

import com.example.employeemanagement.redis.EmployerCache;
import com.example.employeemanagement.redis.UserCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FindShift {
    private Long id;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<UserCache> employees = new HashSet<>();
    private int workerLimit;
    private int currentWorkers;
    private UserCache postedBy;
    private EmployerCache employer;

    public void addEmployee(UserCache user) {
        employees.add(user);
    }
}
