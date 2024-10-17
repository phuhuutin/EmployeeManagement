package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.service.ClockInAndOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/clockin")
public class ClockInOutRecordController {
    @Autowired
    private ClockInAndOutService clockInAndOutService;
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")

    @PostMapping("/{shiftId}")
    public ClockInOutRecord clockIn( @PathVariable Long shiftId ){
        return clockInAndOutService.clockIn(shiftId);
    }
}
