package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.repository.AttendanceRecordRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceRecordService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceRecordService.class);

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRecord save(AttendanceRecord attendanceRecord){
        return attendanceRecordRepository.save(attendanceRecord);
    }
    public AttendanceRecord findById(Long id){
        return attendanceRecordRepository.findById(id).orElseThrow(() -> new RuntimeException("AttendanceRecord not found"));
    }

    public void delete(AttendanceRecord record){
        attendanceRecordRepository.delete(record);
    }

    public boolean deletebyId(Long recordId){
        try{
            AttendanceRecord record = findById(recordId);
            delete(record);
            return true;
        } catch (Exception e){
            //nothing need to be done.
            return false;
        }
    }
}
