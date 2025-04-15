package com.example.employeemanagement.redis;

import com.example.employeemanagement.dto.FindShift;
import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@RedisHash(value = "find_shifts", timeToLive = 20) // Cache for 1 hour
public class FindShiftCache implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String CACHE_NAME = "find_shifts";
    public static final Integer CACHE_TTL = 10; //in minutes
    @Id
    private Long id;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean shiftFull;
    private Set<Long> employeesId = new HashSet<>(); // ✅ Use UserCache instead of full User object
    private int workerLimit;
    private int currentWorkers;
    private Long postedByUserId; // ✅ Store only User ID for postedBy (instead of full User object)
    private Long employerId; // ✅ Store only Employer ID, fetch full Employer details when needed

    public FindShiftCache(Long id, LocalDate date, LocalDateTime startTime, LocalDateTime endTime, boolean shiftFull,
                          Set<Long> employees, int workerLimit, int currentWorkers, Long postedByUserId, Long employerId) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.employeesId = employees;
        this.workerLimit = workerLimit;
        this.currentWorkers = currentWorkers;
        this.postedByUserId = postedByUserId;
        this.employerId = employerId;
    }


    public FindShift toFindShift() {
        FindShift shift = new FindShift();
        shift.setId(this.id);
        shift.setDate(this.date);
        shift.setStartTime(this.startTime);
        shift.setEndTime(this.endTime);
        shift.setWorkerLimit(this.workerLimit);
        shift.setCurrentWorkers(this.currentWorkers);
        // Set Employees and Employer later
//        shift.setEmployees();
        return shift;
    }

    public void addEmployee(Long userId) {
        this.employeesId.add(userId);
    }


}
