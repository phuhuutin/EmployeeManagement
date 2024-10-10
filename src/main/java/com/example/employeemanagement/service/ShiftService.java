package com.example.employeemanagement.service;

import com.example.employeemanagement.Utils.DataUtils;
import com.example.employeemanagement.controller.UserController;
import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.ShiftRepository;
import com.example.employeemanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class ShiftService {
    @Autowired
    private  ShiftRepository shiftRepository;
    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);



    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    public Shift saveShift(ShiftDTO shiftDTO) {
        return shiftRepository.save(mapToShift(shiftDTO));
    }

    // The mapping method belongs to the service
    private Shift mapToShift(ShiftDTO shiftDTO) {
        Shift shift = new Shift();
        shift.setDate(shiftDTO.getDate());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setWorkerLimit(shiftDTO.getWorkerLimit());

        User manager = userRepository.findById(shiftDTO.getPostedById())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + shiftDTO.getPostedById() + " not found"));
        shift.setPostedBy(manager);

        return shift;
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }


    public List<Shift> getShiftsPostedInLatestWeek() {
        LocalDate startOfWeek = DataUtils.getStartOfPreviousWeek();
        logger.info(startOfWeek.toString());
        LocalDate endOfWeek = DataUtils.getEndOfPreviousWeek();
        logger.info(endOfWeek.toString());
        return shiftRepository.findShiftsPostedInLatestWeek(startOfWeek, endOfWeek);
    }

    public List<Payroll> weeklyPayEvaluate(){
        List<Shift> shifts = this.getShiftsPostedInLatestWeek();
        return payEvaluate(shifts);
    }
    public List<Payroll> payEvaluate( List<Shift> shifts){
        Map<User, Payroll> payrollMap = new HashMap<>();
        for (Shift shift : shifts) {
            List<User> pickedShiftUser = shift.getEmployees();
            for(ClockInOutRecord record : shift.getClockInOutRecords()){
                //to  check if the user is scheduled to work on that shift before pay
                if(pickedShiftUser.contains(record.getUser())){
                    User user = record.getUser();
                    Payroll payroll = payrollMap.getOrDefault(user,
                            new Payroll(DataUtils.getStartOfPreviousWeek(), DataUtils.getEndOfPreviousWeek()));
                    payroll.setUser(user);
                    payroll.setPayRate(user.getPayRate());
                    BigDecimal bd = BigDecimal.valueOf(record.getMinuteWorked() / 60).setScale(2, RoundingMode.HALF_UP);
                    payroll.addtotalHoursWorked(bd.doubleValue());
                    bd = BigDecimal.valueOf(user.getPayRate()/60 * record.getMinuteWorked()).setScale(2, RoundingMode.HALF_UP);
                    payroll.addtotalPay(bd.doubleValue());
                    payrollMap.put(user, payroll);

                }
            }
        }
        return new ArrayList<>(payrollMap.values());
    }

    /**
     * 10 minutes for Late and leave early.
     *
     * @param shifts
     * @return
     */
    public List<AttendanceRecord> attendanceEvaluate(List<Shift> shifts ){
        List<AttendanceRecord> result = new ArrayList<>();
        for(Shift shift: shifts){
            List<User> userScheduledList = shift.getEmployees();
            for(ClockInOutRecord clock: shift.getClockInOutRecords()){
                long startShiftOffset = Duration.between(shift.getStartTime(), clock.getClockInTime()).toMinutes();
                //if the user was late for more than 10 minutes
                if(startShiftOffset > 10){
                    //handle late
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LATE);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                   // clock.getUser().getAttendancePoints().addNewRecord(record);
                    result.add(record);
                }

                long endShiftOffset = Duration.between(clock.getClockOutTime(), shift.getEndTime()).toMinutes();
                //if the user clocks out more than 10 mins early
                if(endShiftOffset >= 10){
                    //handle clocks out early
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LEAVEEARLY);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                   // clock.getUser().getAttendancePoints().addNewRecord(record);
                    result.add(record);
                }

                userScheduledList.remove(clock.getUser());

            }

            //completely miss the shift
            userScheduledList.forEach(user -> {
                logger.info(user.getId() + "missed shift" + shift.getId());
                AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.ABSENT);
                record.setAttendancePoints(user.getAttendancePoints());
               // user.getAttendancePoints().addNewRecord(record);
                result.add(record);
            });
        }

        return result;
    }

    public List<AttendanceRecord> weeklyAttendanceEvaluate(){
        List<Shift> shifts = this.getShiftsPostedInLatestWeek();
        return attendanceEvaluate(shifts);
    }

    public String addEmployeeToShift(Long shiftId) {
        try {
            // Get the currently authenticated user from the SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();  // Retrieves the username of the authenticated user

            // Fetch the user (employee) from the repository using the username
            User employee = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

            // Fetch the shift by ID
            Shift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));

            // Check if the shift is full
            if (shift.isShiftFull()) {
                logger.warn("Shift with ID {} is full. Cannot add employee with ID {}", shiftId, employee.getId());
                return "Shift is already full!";
            }

            // Add employee to the shift
            boolean added = shift.addEmployee(employee);
            if (!added) {
                return "Failed to add employee to the shift.";
            }

            // Save the updated shift
            shiftRepository.save(shift);

            logger.info("Employee with ID {} successfully added to shift with ID {}", employee.getId(), shiftId);
            return "Employee successfully added to the shift.";

        } catch (Exception e) {
            logger.error("Error adding employee to shift with ID {}: {}", shiftId, e.getMessage());
            return "Error adding employee to the shift: " + e.getMessage();
        }
    }
}
