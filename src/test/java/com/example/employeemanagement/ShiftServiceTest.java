package com.example.employeemanagement;

import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.service.ShiftService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ShiftServiceTest {

    @InjectMocks
    private ShiftService shiftService; // Class under test
            User employee1 = new User();
            User employee2 = new User();

            Shift shift1 = new Shift();

            Shift shift2 = new Shift();

            ClockInOutRecord record1shift1 = new ClockInOutRecord(); // employee1's record on shift 1
            ClockInOutRecord record2shift1 = new ClockInOutRecord();

            ClockInOutRecord record1shift2 = new ClockInOutRecord();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        //setup employee1
        employee1.setId(1L);
        employee1.setUsername("john_doe1");
        employee1.setEmail("john_doe1@example.com");
        employee1.setPayRate(15.0);
        employee1.setRole("EMPLOYEE");

        //setup employee2
        employee2.setId(2L);
        employee2.setUsername("john_doe2");
        employee2.setEmail("john_doe2@example.com");
        employee2.setPayRate(23.25);
        employee2.setRole("EMPLOYEE");

        // Set basic shift details
        shift1.setDate(LocalDate.now()); // Today's date
        shift1.setStartTime(LocalDateTime.of(2024, 10, 10, 9, 0)); // Start time: 10th Oct 2024, 9:00 AM
        shift1.setEndTime(LocalDateTime.of(2024, 10, 10, 17, 0)); // End time: 10th Oct 2024, 5:00 PM
        shift1.setWorkerLimit(5); // Max 5 workers for this shift

        shift2.setDate(LocalDate.now()); // Today's date
        shift2.setStartTime(LocalDateTime.of(2024, 11, 10, 9, 0)); // Start time: 11th Oct 2024, 9:00 AM
        shift2.setEndTime(LocalDateTime.of(2024, 11, 10, 17, 0)); // End time: 11th Oct 2024, 5:00 PM
        shift2.setWorkerLimit(5); // Max 5 workers for this shift

        //set up clock in and out record
        record1shift1.setId(1L);
        record1shift1.setUser(employee1);
        record1shift1.setClockInTime(LocalDateTime.of(2024, 10, 10, 9, 0));  // Clock in at shift start
        record1shift1.setClockOutTime(LocalDateTime.of(2024, 10, 10, 16, 55));
        record1shift1.setShift(shift1);

        record1shift2.setId(3L);
        record1shift2.setUser(employee1);
        record1shift2.setClockInTime(LocalDateTime.of(2024, 11, 10, 9, 0));  // Clock in at shift start
        record1shift2.setClockOutTime(LocalDateTime.of(2024, 11, 10, 16, 55));
        record1shift2.setShift(shift2);


        record2shift1.setId(2L);
        record2shift1.setUser(employee2);
        record2shift1.setClockInTime(LocalDateTime.of(2024, 10, 10, 9, 0));  // Clock in at shift start
        record2shift1.setClockOutTime(LocalDateTime.of(2024, 10, 10, 16, 56));
        record2shift1.setShift(shift1);




    }

//    @Test
//    public void testSimplePayEvaluate() {
//        List<Shift> shifts = new ArrayList<>();
//        //employee1 and 2 are scheduled to work on this shift
//        shift1.addEmployee(employee1);
//        shift1.addEmployee(employee2);
//
//        // Create ClockInOutRecord for  employee 1
//        shift1.addClockInRecord(record1shift1);
//        shift1.addClockInRecord(record2shift1);
////
////
////        shifts.add(shift1);
//
//        List<ClockInOutRecord> clocks = shiftService.payEvaluate(shift1);
//        clocks.sort(Comparator.comparingLong(clock -> clock.getUser().getId())
//        );
//        List<AttendanceRecord> records = shiftService.attendanceEvaluateViaClock(record1shift1);
//        assertTrue(records.isEmpty());
//        records = shiftService.attendanceEvaluateViaClock(record2shift1);
//        assertTrue(records.isEmpty());
//
//
//        assert clocks.get(0).getPayroll() != null;
//        assertEquals(clocks.get(0).getPayroll().getTotalPay(), employee1.getPayRate()/60* Duration.between(record1shift1.getClockInTime(), record1shift1.getClockOutTime()).toMinutes()); // Check the pay rate is correct
//        BigDecimal db = BigDecimal.valueOf(employee2.getPayRate()/60* Duration.between(record2shift1.getClockInTime(), record2shift1.getClockOutTime()).toMinutes()).setScale(2, RoundingMode.HALF_UP);
//        assert clocks.get(1).getPayroll() != null;
//        assertEquals(clocks.get(1).getPayroll().getTotalPay(), db.doubleValue()); // Check the pay rate is correct
//
//    }

    @Test
    public void testZeroPayAndABSENTAttandanceEvaluate(){
        //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);
        ClockInOutRecord clock1 = new ClockInOutRecord();
        clock1.setUser(employee1);
        clock1.setShift(shift1);
        shift1.addClockInRecord(clock1);

        List<ClockInOutRecord> clock = shiftService.payEvaluate(shift1);

        assertEquals(0, clock.get(0).getPayroll().getTotalPay());
        assert clock.get(0).getAttendanceRecords() != null;
        List<AttendanceRecord> records = shiftService.attendanceEvaluateViaClock(clock1);

        assertEquals(AttendanceReason.ABSENT,records.get(0).getReason());

    }

    @Test
    //an employee works without scheduling.
    public void testNoScheduleWorkPayEvaluate(){
        //employee1 is scheduled to work on this shift
      //  shift1.addEmployee(employee1);

        //add clock in and out.
        shift1.addClockInRecord(record1shift1);
        List<ClockInOutRecord> clocks = shiftService.payEvaluate(shift1);
        assertEquals(0, clocks.get(0).getPayroll().getTotalPay());

    }


    @Test
    public void testLATEAttandanceEvaluate(){
        //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);

        //set the record to be late 40 minutes
        record1shift1.setClockInTime(shift1.getStartTime().plusMinutes(40));
        record1shift1.setShift(shift1);
        record1shift1.setUser(employee1);
        shift1.addClockInRecord(record1shift1);
        List<AttendanceRecord> attendanceRecords = shiftService.attendanceEvaluateViaClock(record1shift1);
        assertEquals(AttendanceReason.LATE,attendanceRecords.get(0).getReason());

    }

    @Test
    public void testEARLYAttandanceEvaluate(){
         //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);

        //set the record to be late 40 minutes
        record1shift1.setClockOutTime(shift1.getEndTime().minusMinutes(40));
        shift1.addClockInRecord(record1shift1);

        List<AttendanceRecord> attendanceRecords = shiftService.attendanceEvaluateViaClock(record1shift1);
        assertEquals(AttendanceReason.LEAVEEARLY,attendanceRecords.get(0).getReason());


    }




}
