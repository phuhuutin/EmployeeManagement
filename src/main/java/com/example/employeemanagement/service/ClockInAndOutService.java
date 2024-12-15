package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.ClockInOutRecordRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClockInAndOutService {
     private final ShiftService shiftService;
     private final UserService userService;
     private final ClockInOutRecordRepository clockInOutRecordRepository;


    public ClockInOutRecord save(ClockInOutRecord clock){
        return clockInOutRecordRepository.save(clock);
    }
    public void delete(ClockInOutRecord clock){
        clockInOutRecordRepository.delete(clock);
    }

    public ClockInOutRecord findById(Long id){
        return clockInOutRecordRepository.findById(id).orElseThrow();
    }

    public ClockInOutRecord clockIn(Long shiftId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Shift shift = shiftService.getShiftById(shiftId);
        if(!LocalDate.now().equals(shift.getDate())){
            throw new IllegalArgumentException("You can't clock for this shift today.");
        }

        User user = userService.getUserByUsername(username);
        List<ClockInOutRecord> clockList = shift.getClockInOutRecords();
        ClockInOutRecord clock = null;
        for(ClockInOutRecord c: shift.getClockInOutRecords()){
            if(c.getUser().equals(user)) {
                clock = c;
                break;
            }
        }


        if(clock == null)
            throw new IllegalArgumentException("Something went wrong, could not find clock.");
        if(!shift.getEmployees().contains(user)){
            throw new IllegalArgumentException("You can't clock for this shift");
        }
        if(clock.getClockInTime() == null)
            clock.setClockInTime(LocalDateTime.now());
        else
            clock.setClockOutTime(LocalDateTime.now());
        clockInOutRecordRepository.save(clock);
        return clock;

    }


}
