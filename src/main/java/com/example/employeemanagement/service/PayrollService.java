package com.example.employeemanagement.service;

import com.example.employeemanagement.controller.UserController;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.PayrollRepository;
import com.example.employeemanagement.repository.ShiftRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PayrollService {
    private final ShiftRepository shiftRepository;
    private final PayrollRepository payrollRepository;
    private final ShiftService shiftService;
    private final ClockInAndOutService clockInAndOutService;
    private final AttendanceRecordService attendanceRecordService;

    public void savePayroll(Payroll payroll){
        payrollRepository.save(payroll);
    }
    public List<Payroll> findPayrollsbyUserId(Long userId){
        return payrollRepository.findByUserId(userId);
    }
    @Transactional
    public List<ClockInOutRecord> evaluateASingleShift(Long shiftId){
        List<ClockInOutRecord> clockListAfterEval = shiftService.singleShiftPayEvaluate(shiftId);
        try{
            clockListAfterEval.forEach(clock -> {
                if(clock.getPayroll() != null)
                    payrollRepository.save(clock.getPayroll());
               // clockInAndOutService.save(clock);
            });
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + ". Failed to save payroll");
        }

        clockListAfterEval = shiftService.singleShiftAttendanceEvaluate(shiftId);
        try{
            clockListAfterEval.forEach(clock -> {
                if(clock.getAttendanceRecords() != null)
                    clock.getAttendanceRecords().forEach(attendanceRecordService::save
                     );
            });
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + ". Failed to save AttendanceRecord");
        }
        return clockListAfterEval;
    }

}
