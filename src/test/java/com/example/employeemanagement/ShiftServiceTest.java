package com.example.employeemanagement;

import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.service.ShiftService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testSimplePayEvaluate() {
        List<Shift> shifts = new ArrayList<>();
        //employee1 and 2 are scheduled to work on this shift
        shift1.addEmployee(employee1);
        shift1.addEmployee(employee2);

        // Create ClockInOutRecord for  employee 1
        shift1.addClockInRecord(record1shift1);
        shift1.addClockInRecord(record2shift1);


        shifts.add(shift1);

        List<Payroll> payrolls = shiftService.payEvaluate(shifts);
        payrolls.sort(Comparator.comparingLong(payroll -> payroll.getUser().getId())
        );
        List<AttendanceRecord> records = shiftService.attendanceEvaluate(shifts);
        assertEquals(0, records.size()); // no record found because both employee are on one.


        assertEquals(payrolls.get(0).getTotalPay(), employee1.getPayRate()/60* Duration.between(record1shift1.getClockInTime(), record1shift1.getClockOutTime()).toMinutes()); // Check the pay rate is correct
        BigDecimal db = BigDecimal.valueOf(employee2.getPayRate()/60* Duration.between(record2shift1.getClockInTime(), record2shift1.getClockOutTime()).toMinutes()).setScale(2, RoundingMode.HALF_UP);
        assertEquals(payrolls.get(1).getTotalPay(), db.doubleValue()); // Check the pay rate is correct

    }

    @Test
    public void testZeroPayAndABSENTAttandanceEvaluate(){
        List<Shift> shifts = new ArrayList<>();
        //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);

        // no record1
        //shift1.addClockInRecord(record1);

        shifts.add(shift1);

        List<Payroll> payrolls = shiftService.payEvaluate(shifts);
        List<AttendanceRecord> attendanceRecords = shiftService.attendanceEvaluate(shifts);

        assertEquals(0,payrolls.size());
        assertEquals(AttendanceReason.ABSENT,attendanceRecords.get(0).getReason());

    }

    @Test
    //an employee works without scheduling.
    public void testNoScheduleWorkPayEvaluate(){
        List<Shift> shifts = new ArrayList<>();
        //employee1 is scheduled to work on this shift
      //  shift1.addEmployee(employee1);

        //add clock in and out.
        shift1.addClockInRecord(record1shift1);

        shifts.add(shift1);

        List<Payroll> payrolls = shiftService.payEvaluate(shifts);
        assertEquals(0,payrolls.size());

    }
    @Test
    public void testMutipleShiftSimplePayEvaluate(){
        List<Shift> shifts = new ArrayList<>();
        shift1.addEmployee(employee1);
        shift2.addEmployee(employee1);

        shift1.addClockInRecord(record1shift1);
        shift2.addClockInRecord(record1shift2);
        shifts.add(shift1);
        shifts.add(shift2);

        List<Payroll> payrolls = shiftService.payEvaluate(shifts);

        assertEquals(payrolls.get(0).getTotalPay(),
                employee1.getPayRate()/60* Duration.between(record1shift1.getClockInTime(), record1shift1.getClockOutTime()).toMinutes()
                +   employee1.getPayRate()/60* Duration.between(record1shift2.getClockInTime(), record1shift2.getClockOutTime()).toMinutes()
        );
    }

    @Test
    public void testLATEAttandanceEvaluate(){
        List<Shift> shifts = new ArrayList<>();
        //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);

        //set the record to be late 40 minutes
        record1shift1.setClockInTime(shift1.getStartTime().plusMinutes(40));
        shift1.addClockInRecord(record1shift1);

        shifts.add(shift1);
        List<AttendanceRecord> attendanceRecords = shiftService.attendanceEvaluate(shifts);
        assertEquals(AttendanceReason.LATE,attendanceRecords.get(0).getReason());

    }

    @Test
    public void testEARLYAttandanceEvaluate(){
        List<Shift> shifts = new ArrayList<>();
        //employee1 is scheduled to work on this shift
        shift1.addEmployee(employee1);

        //set the record to be late 40 minutes
        record1shift1.setClockOutTime(shift1.getEndTime().minusMinutes(40));
        shift1.addClockInRecord(record1shift1);

        shifts.add(shift1);
        List<AttendanceRecord> attendanceRecords = shiftService.attendanceEvaluate(shifts);
        assertEquals(AttendanceReason.LEAVEEARLY,attendanceRecords.get(0).getReason());


    }




}
