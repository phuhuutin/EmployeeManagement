package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttendanceRecordService {
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRecord save(AttendanceRecord attendanceRecord){
        return attendanceRecordRepository.save(attendanceRecord);
    }
}
