package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.dto.SingleUserShiftData;
import com.example.employeemanagement.entity.ClockInOutRecord;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.service.ClockInAndOutService;
import com.example.employeemanagement.service.ManagerService;
import com.example.employeemanagement.service.ShiftService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/shift")
@AllArgsConstructor
@Log4j2
public class ShiftController {
    private final ShiftService shiftService;
    private final ManagerService managerService;
    private ClockInAndOutService clockInAndOutService;

    @PreAuthorize("hasAuthority('MANAGER')")  // Only allow users with 'MANAGER' authority
    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }

    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @GetMapping("/getshifts/{employerId}")
    public List<Shift> getAllShiftsAfterNow(@PathVariable Long employerId) {
        return shiftService.findShiftAfterNow(employerId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<?> getAllShiftsByUser() {
        try {
            List<SingleUserShiftData> shifts = shiftService.getAllShiftsByUser();
            return ResponseEntity.ok(shifts);
        } catch (NoResourceFoundException e) {
            // Handle specific custom exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Could not find/access the shifts for the current user");
        } catch (Exception e) {
            // Handle any other general exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }


    @GetMapping("/{id}")
    public Shift getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id);
    }

    @PreAuthorize("hasAuthority('MANAGER')")  // Only allow users with 'MANAGER' authority
    @PostMapping
    public ResponseEntity<String> createShift(@RequestBody ShiftDTO shiftDTO) {
        if(Duration.between(LocalDateTime.now(),shiftDTO.getStartTime()).toMinutes() <= 0)
            return new ResponseEntity<>("Can not add shift for the past", HttpStatus.BAD_REQUEST);
        try {
            String savedShift = managerService.createAshiftAndScheduleEvaluation(shiftDTO);
            return new ResponseEntity<>(savedShift, HttpStatus.CREATED);
        } catch (Exception e) {
            // Catch any exceptions and return an error response
            String errorMessage = "Error occurred while creating the shift: " + e.getMessage();
            // Return the error message along with the 500 INTERNAL_SERVER_ERROR status
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAuthority('MANAGER')")  // Only allow users with 'MANAGER' authority
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteShift(@PathVariable Long id) {
        try {
            String message = managerService.deleteShiftById(id);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch(AuthorizationServiceException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            // Catch any exceptions and return an error response
            String errorMessage = "Error occurred while deleting the shift: " + e.getMessage();
            // Return the error message along with the 500 INTERNAL_SERVER_ERROR status
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/{shiftId}/addEmployee")
    public ResponseEntity<String> addEmployeeToShift(@PathVariable Long shiftId) {
        try {
            shiftService.addEmployeeToShift(shiftId);
            return new ResponseEntity<>("The employee is added to the shift.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // User or shift not found
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Shift is full or cannot add employee
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/{shiftId}/drop")
    public ResponseEntity<String> dropShift(@PathVariable Long shiftId) {
        try {
            shiftService.dropShift(shiftId);
            return new ResponseEntity<>("The employee dropped the shift.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/clock/{shiftId}")
    public ResponseEntity<String> clockIn( @PathVariable Long shiftId ){
        try{
            clockInAndOutService.clockIn(shiftId);
            return new ResponseEntity<>("Successfully clock in/out", HttpStatus.OK);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("can not clock today");
        } catch (Exception e){
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }











}
