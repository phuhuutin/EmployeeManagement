package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final PayrollService payrollService;
    private final ClockInAndOutService clockInAndOutService;
    private final ShiftService shiftService;
    private final UserService userService;
    private final AttendanceRecordService attendanceRecordService;

    /**
     * Add a shift and then schedule a shift evaluation to run one hour after the shift ends.
     * @param shiftDTO
     */
    public String createAshiftAndScheduleEvaluation(ShiftDTO shiftDTO) {
        Shift shift = shiftService.saveShift(shiftDTO);

        JobId jobId = BackgroundJob.schedule(
                shift.getEndTime().plusHours(24), // Schedule the evaluation 24 hours after the shift.
                ()-> processAttendanceRecords(shift.getId())
        );
        shift.setJobId(jobId.asUUID());
        shiftService.saveShift(shift);
        return "Successfully add a shift on " + shift.getStartTime().toLocalDate().toString();
    }

    public void processAttendanceRecords(Long shiftId) {
        List<ClockInOutRecord> clocks = payrollService.evaluateASingleShift(shiftId);
        clocks.forEach(clock -> {
            if (clock.getAttendanceRecords() != null && !clock.getAttendanceRecords().isEmpty()) {
                clock.getAttendanceRecords().forEach(this::set30DaysExpirationForAttandanceRecord);
            }
        });
    }

    public void set30DaysExpirationForAttandanceRecord(AttendanceRecord record){
        JobId jobId = BackgroundJob.schedule(
                record.getDate().atStartOfDay().plusMonths(1), // 30days
                ()->this.attendanceRecordService.deletebyId(record.getId()) // Call the job method
        );
        record.setJobId(jobId.asUUID());
        attendanceRecordService.save(record);
    }

    public String testEvaluate() {
        BackgroundJob.schedule(
                LocalDateTime.now().plusSeconds(30),
                //  shift.getEndTime().plusHours(1), // Schedule 30 seconds from now
                ()->this.payrollService.evaluateASingleShift(7L) // Call the job method
        );
        return "Successfully add a shift on ";
    }

    @Transactional
    public String deleteShiftById(Long shiftId){
            // Fetch the shift by ID
            Shift shift = shiftService.findShiftById(shiftId);
            if (shift == null) {
                throw new IllegalArgumentException("Shift not found with ID: " + shiftId);
            }
            if((LocalDateTime.now().plusHours(12)).isAfter(shift.getStartTime())){
                throw new IllegalArgumentException("Can not delete the shift within 12 hours of starting." + shift.getStartTime() + "--" + LocalDateTime.now());
            }
            User currentUser = userService.getCurrentUser();
            if(!currentUser.equals(shift.getPostedBy())){
                throw new AuthorizationServiceException("You're not authorized to delete this shift.");
            }
            shift.getEmployees().forEach(u ->{
                u.getPickedShifts().remove(shift);
                userService.saveUser(u);
            });

            shift.getClockInOutRecords().forEach(clockInAndOutService::delete);
            // Retrieve the jobId
            UUID jobId = shift.getJobId();
            if (jobId == null) {
                throw new IllegalStateException("No associated jobId found for Shift with ID: " + shiftId);
            }

            // Delete the shift
            if(shiftService.deleteShiftbyId(shiftId))
            // Delete the background job
                BackgroundJob.delete(jobId);
            else
                throw new IllegalArgumentException("Something went wrong");
            return "The shift is deleted successfully.";

    }
}