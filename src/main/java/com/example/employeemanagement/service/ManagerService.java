package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.entity.Shift;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ManagerService {

    // Define the Hello World job method
    @Autowired
    private PayrollService payrollService;
    @Autowired
    private ShiftService shiftService;

    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);

    /**
     * Add a shift and then schedule a shift evaluation to run one hour after the shift ends.
     * @param shiftDTO
     */
    public String createAshiftAndScheduleEvaluation(ShiftDTO shiftDTO) {

        Shift shift = shiftService.saveShift(shiftDTO);

        JobId jobId = BackgroundJob.schedule(
                shift.getEndTime().plusHours(1), // Schedule 30 seconds from now
                ()->this.payrollService.weekPayEvaluation() // Call the job method
        );
        shift.setJobId(jobId.asUUID());
        shiftService.saveShift(shift);
        return "Successfully add a shift on " + shift.getStartTime().toLocalDate().toString();
    }


    public String deleteShiftById(Long shiftId){
        try {
            // Fetch the shift by ID
            Shift shift = shiftService.findShiftById(shiftId);
            if (shift == null) {
                throw new IllegalArgumentException("Shift not found with ID: " + shiftId);
            }

            // Retrieve the jobId
            UUID jobId = shift.getJobId();
            if (jobId == null) {
                throw new IllegalStateException("No associated jobId found for Shift with ID: " + shiftId);
            }

            // Delete the shift
            shiftService.deleteShiftbyId(shiftId);

            // Delete the background job
            BackgroundJob.delete(new JobId(jobId));

            return "Shift and associated job deleted successfully.";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Log the error and throw a specific exception back to the controller
            logger.error("Error deleting shift with ID {}: {}", shiftId, e.getMessage());
            throw e; // Re-throw the caught exception to the controller
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error occurred while deleting shift with ID {}: {}", shiftId, e.getMessage());
            throw new RuntimeException("Error deleting shift and job: " + e.getMessage());
        }

    }
}