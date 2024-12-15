package com.example.employeemanagement.service;

import com.example.employeemanagement.Utils.DataUtils;
import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.dto.SingleUserShiftData;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShiftService {
     private final ShiftRepository shiftRepository;
     private final UserRepository userRepository;
     private final EmployerRepository employerRepository;
     private final ClockInOutRecordRepository clockInOutRecordRepository;
     private final PayrollRepository payrollRepository;


    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public List<SingleUserShiftData> getAllShiftsByUser() throws NoResourceFoundException {
        List<SingleUserShiftData> re = new ArrayList<>();
        // Get the currently authenticated user from the SecurityContext

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET,"Could not find/access the current user"));

        for(Shift shift : user.getPickedShifts()){
            SingleUserShiftData data = new SingleUserShiftData();
            data.setId(shift.getId());
            data.setUsername(user.getUsername());
            data.setDate(shift.getDate());
            data.setStartTime(shift.getStartTime());
            data.setEndTime(shift.getEndTime());
            data.setEmployer(user.getEmployer());
            for(ClockInOutRecord clock : shift.getClockInOutRecords()) {
                if (clock.getUser().equals(user)) {
                    data.setClock(clock);
                    break;
                }
            }

            re.add(data);
        }

        return re;

    }

    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    public Shift saveShift(ShiftDTO shiftDTO) {
        Employer employer = employerRepository.findById(shiftDTO.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("Can not find the given employer"));

        return shiftRepository.save(mapToShift(shiftDTO, employer));
    }
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public boolean deleteShiftbyId(Long shiftId){
        try{
            shiftRepository.deleteById(shiftId);
            return true;
        }catch (Exception e){
            return false;
        }

    }

    // The mapping method belongs to the service
    private Shift mapToShift(ShiftDTO shiftDTO, Employer employer) {
        Shift shift = new Shift();
        shift.setDate(shiftDTO.getDate());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setWorkerLimit(shiftDTO.getWorkerLimit());

        User manager = userRepository.findById(shiftDTO.getPostedById())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + shiftDTO.getPostedById() + " not found"));
        shift.setPostedBy(manager);
        shift.setEmployer(employer);
        return shift;
    }


    public List<ClockInOutRecord> payEvaluate( Shift shift){
        List<ClockInOutRecord> clockWithCalculatedPayRoll = new ArrayList<>();

            Set<User> pickedShiftUser = shift.getEmployees();
            for(ClockInOutRecord record : shift.getClockInOutRecords()){

                if(record.getClockInTime() == null && record.getClockOutTime() == null){
                    User user = record.getUser();
                    Payroll payroll = new Payroll();
                    payroll.setShift_id(shift.getId());
                    payroll.setUser(user);
                    payroll.setPayRate(user.getPayRate());
                    payroll.addtotalPay(0);
                    record.setPayroll(payroll);
                    clockWithCalculatedPayRoll.add(record);
                    break;
                }else{
                    User user = record.getUser();
                    Payroll payroll = new Payroll();
                    payroll.setShift_id(shift.getId());
                    payroll.setUser(user);
                    payroll.setPayRate(user.getPayRate());
                    BigDecimal bd = BigDecimal.valueOf(record.getMinuteWorked() / 60).setScale(2, RoundingMode.HALF_UP);
                    payroll.addtotalHoursWorked(bd.doubleValue());
                    if(pickedShiftUser.contains(record.getUser()) ){
                        bd = BigDecimal.valueOf(user.getPayRate()/60 * record.getMinuteWorked()).setScale(2, RoundingMode.HALF_UP);
                    }else{
                        bd = BigDecimal.valueOf(0);
                    }
                    payroll.addtotalPay(bd.doubleValue());
                    record.setPayroll(payroll);
                    clockWithCalculatedPayRoll.add(record);
                }






            }

        return clockWithCalculatedPayRoll;
    }


    public Payroll singlePayEvaluate(ClockInOutRecord clock){
            Payroll payroll = clock.getPayroll();
            assert payroll != null;
            BigDecimal bd = BigDecimal.valueOf(clock.getMinuteWorked() / 60).setScale(2, RoundingMode.HALF_UP);
            payroll.setTotalHoursWorked(bd.doubleValue());
            bd = BigDecimal.valueOf(payroll.getPayRate()/60 * clock.getMinuteWorked()).setScale(2, RoundingMode.HALF_UP);
            payroll.setTotalPay(bd.doubleValue());
            return payroll;
    }
    public Shift findShiftById(Long shiftId){
        // Fetch the shift by ID
        return shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));
    }
    public List<ClockInOutRecord> singleShiftPayEvaluate(Long shiftId){
        Optional<Shift> shiftOptional = shiftRepository.findById(shiftId);
        Shift shift;
        if(shiftOptional.isPresent()){
            shift = shiftOptional.get();
            return this.payEvaluate(shift);
        }else{
            throw new ResourceAccessException("Error finding the shift with " + shiftId);
        }
    }





    public List<AttendanceRecord> attendanceEvaluateViaClock(ClockInOutRecord clock){
        List<AttendanceRecord> result = new ArrayList<>();
        Shift shift = clock.getShift();

        if(clock.getClockInTime() == null &&  clock.getClockOutTime() == null){
            AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.ABSENT);
            record.setAttendancePoints(clock.getUser().getAttendancePoints());
            record.setClockInOutRecord(clock);
            result.add(record);
            return result;
        }

                long startShiftOffset = Duration.between(shift.getStartTime(), clock.getClockInTime()).toMinutes();
                //if the user was late for more than 10 minutes
                if(startShiftOffset > 10){
                    //handle late
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LATE);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                    // clock.getUser().getAttendancePoints().addNewRecord(record);
                    record.setClockInOutRecord(clock);
                    result.add(record);
                }

                if(clock.getClockOutTime() != null){
                    long endShiftOffset = Duration.between(clock.getClockOutTime(), shift.getEndTime()).toMinutes();
                    //if the user clocks out more than 10 mins early
                    if(endShiftOffset >= 10){
                        //handle clocks out early
                        AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LEAVEEARLY);
                        record.setAttendancePoints(clock.getUser().getAttendancePoints());
                        // clock.getUser().getAttendancePoints().addNewRecord(record);
                        record.setClockInOutRecord(clock);
                        result.add(record);
                    }
                }else{
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LEAVEEARLY);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                    // clock.getUser().getAttendancePoints().addNewRecord(record);
                    result.add(record);
                }

        return result;
    }



    public List<ClockInOutRecord> singleShiftAttendanceEvaluate(Long shiftId ){
        Optional<Shift> shiftOptional = shiftRepository.findById(shiftId);
        Shift shift;
        if(shiftOptional.isPresent()){
            shift = shiftOptional.get();
            List<ClockInOutRecord> clocks = shift.getClockInOutRecords();
            for(ClockInOutRecord clock: clocks){
                List<AttendanceRecord> records = attendanceEvaluateViaClock(clock);
                clock.setAttendanceRecords(records);
            }
            return clocks;

        }else{
            throw new ResourceAccessException("Error finding the shift with " + shiftId);
        }
    }


    public void addEmployeeToShift(Long shiftId) {
        // Get the currently authenticated user from the SecurityContext



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user

        // Fetch the user (employee) from the repository using the username
        User employee = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        // Fetch the shift by ID
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));
        if(!employee.getEmployer().equals(shift.getEmployer())){
            throw new IllegalStateException("You do not work for this employer.");
        }
        if(shift.getEmployees().contains(employee)){
            throw new IllegalStateException("User already picked this shift.");
        }
        if(LocalDateTime.now().isAfter(shift.getStartTime()))
            throw new IllegalStateException("You can not add the started shift.");

            //create empty clock
        ClockInOutRecord clock = new ClockInOutRecord();
        clock.setShift(shift);
        clock.setUser(employee);
        clock = clockInOutRecordRepository.save(clock);


        // Check if the shift is full
        if (shift.isShiftFull()) {
             throw new IllegalStateException("Shift is already full!");
        }

        // Add employee to the shift
        boolean added = shift.addEmployee(employee);
        if (!added) {
            throw new IllegalStateException("Failed to add employee to the shift.");
        }

        // Add shift to the employee's picked shifts
        employee.getPickedShifts().add(shift);
        shift.addClockInRecord(clock);
        // Save the updated shift and employee
        shiftRepository.save(shift);
        userRepository.save(employee);
     }

    public List<Shift> findShiftAfterNow(Long employerId){
        return shiftRepository.findAllShiftsAfterNow(LocalDateTime.now(), employerId);
    }
    @Transactional
    public void dropShift(Long shiftId) throws NoResourceFoundException {
        // Get the currently authenticated user from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET,"Could not find/access the current user"));

        // Fetch the shift by ID
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));

        if(LocalDateTime.now().plusDays(1L).isAfter(shift.getStartTime()))
            throw new IllegalArgumentException("It's too late to drop this shift.");
        if(!shift.getEmployees().contains(user))
            throw new IllegalArgumentException("The user is not scheduled for this shift.");
        user.getPickedShifts().remove(shift);
        shift.getEmployees().remove(user);

        // Filter the records to be removed
        ClockInOutRecord recordToRemove = shift.getClockInOutRecords()
                .stream()
                .filter(record -> record.getUser().equals(user))
                .findFirst()
                .orElse(null);
        // Remove from the list
        if (recordToRemove != null) {
            shift.getClockInOutRecords().remove(recordToRemove);
            clockInOutRecordRepository.delete(recordToRemove);
        }
        shift.setCurrentWorkers(shift.getCurrentWorkers() - 1);
        if(shift.getCurrentWorkers() < 0)
            throw new IllegalArgumentException("Something went wrong.");
        // Save the updated shift and employee
        shiftRepository.save(shift);
        userRepository.save(user);
    }

}
