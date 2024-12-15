package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ClockInOutRecordDTO;
import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Payroll;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.service.AttendanceRecordService;
import com.example.employeemanagement.service.ClockInAndOutService;
import com.example.employeemanagement.service.PayrollService;
import com.example.employeemanagement.service.ShiftService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/clock")
@AllArgsConstructor
public class ClockInOutRecordController {
    private ClockInAndOutService clockInAndOutService;
    private AttendanceRecordService attendanceRecordService;
    private PayrollService payrollService;
    private ShiftService shiftService;


    @PreAuthorize( "hasAuthority('MANAGER')")
    @Transactional
    @PutMapping
    public ResponseEntity<?> updateClock(@RequestBody ClockInOutRecordDTO updateClock){
        try{
            ClockInOutRecord clock = clockInAndOutService.findById(updateClock.getId());
            clock.setClockInTime(updateClock.getClockInTime());
            clock.setClockOutTime(updateClock.getClockOutTime());
            Shift shift = shiftService.findShiftById(clock.getShiftId());
            //validation
            if(!shift.getDate().equals(updateClock.getClockInTime().toLocalDate())
                || !shift.getDate().equals(updateClock.getClockOutTime().toLocalDate())){
                return ResponseEntity.badRequest().body("Invalid In/Out time!");
            }
            shift.getClockInOutRecords().remove(clock);
            shift.getClockInOutRecords().add(clock);
            shiftService.saveShift(shift);
            if(clock.getPayroll() != null){
                Payroll payroll = shiftService.singlePayEvaluate(clock);
                payrollService.savePayroll(payroll);
                clock.setPayroll(payroll);
            }
            //attendance
            assert clock.getAttendanceRecords() != null;
            if(!clock.getAttendanceRecords().isEmpty()){
                clock.getAttendanceRecords().forEach(attendanceRecord ->
                        attendanceRecordService.delete(attendanceRecord)
                );
            }

                List<AttendanceRecord> recordList = shiftService.attendanceEvaluateViaClock(clock);
                recordList.forEach(record->{
                    attendanceRecordService.save(record);
                });
                clock.setAttendanceRecords(recordList);
            clockInAndOutService.save(clock);
            return ResponseEntity.ok(clock);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/{shiftId}")
    public ClockInOutRecord clockIn( @PathVariable Long shiftId ){
        return clockInAndOutService.clockIn(shiftId);
    }
}
