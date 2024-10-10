package com.example.employeemanagement.service;

import com.example.employeemanagement.controller.UserController;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.PayrollRepository;
import com.example.employeemanagement.repository.ShiftRepository;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PayrollService {
    @Autowired
    private  ShiftRepository shiftRepository;
    @Autowired
    private PayrollRepository payrollRepository;
    @Autowired
    private ShiftService shiftService;

    @Autowired
    private AttendanceRecordService attendanceRecordService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

//    public List<Shift> evaluatePayAndAttendanceOfTheLastestWeek(){
//        List<Shift> list = shiftService.getShiftsPostedInLatestWeek();
//        logger.info(list.toString());
//        return list;
//    }

    public void savePayroll(Payroll payroll){
        payrollRepository.save(payroll);
    }
    @Transactional
    public List<Payroll> weekPayEvaluation() throws RuntimeException{
        List<Payroll> payrollMap = shiftService.weeklyPayEvaluate();
        // Prepare the result list and save payroll records
        try{
            payrollMap.forEach(this::savePayroll);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + ". Failed to save payroll");
        }

        weeklyAttandanceEvaluation();


        return payrollMap;
    }

    @Transactional
    public List<AttendanceRecord> weeklyAttandanceEvaluation() throws RuntimeException{
        List<AttendanceRecord> attendanceRecords = shiftService.weeklyAttendanceEvaluate();
        // Prepare the result list and save payroll records
        try{
            attendanceRecords.forEach(this.attendanceRecordService::save);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + ". Failed to save AttendanceRecord");
        }

        return attendanceRecords;
    }
}
