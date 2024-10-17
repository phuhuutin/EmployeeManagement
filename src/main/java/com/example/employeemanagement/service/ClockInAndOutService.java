package com.example.employeemanagement.service;

import com.example.employeemanagement.controller.UserController;
import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.ClockInOutRecordRepository;
import com.example.employeemanagement.repository.ShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ClockInAndOutService {
    @Autowired
    private ShiftService shiftService;

    @Autowired
    private  UserService userService;
    @Autowired
    private ClockInOutRecordRepository clockInOutRecordRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClockInAndOutService.class);

    public ClockInOutRecord clockIn(Long shiftId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Shift shift = shiftService.getShiftById(shiftId);
        User user = userService.getUserByUsername(username);
        logger.info(String.valueOf(shift.getId()));
        logger.info(user.getUsername());

        List<ClockInOutRecord> listClock = shift.getClockInOutRecords();
        listClock = listClock.stream().filter(clock -> clock.getUser().equals(user)).toList();
        logger.info(String.valueOf(listClock.size()));

        if(listClock.isEmpty()){
            ClockInOutRecord newRecord = new ClockInOutRecord();
            newRecord.setClockInTime(LocalDateTime.now());
            newRecord.setClockOutTime(LocalDateTime.now());

            newRecord.setUser(user);
            newRecord.setShift(shift);
            clockInOutRecordRepository.save(newRecord);
            logger.debug(newRecord.toString());
             return newRecord;
        }else{
            listClock.get(0).setClockOutTime(LocalDateTime.now());
            clockInOutRecordRepository.save(listClock.get(0));

            return listClock.get(0);
        }


    }


}
