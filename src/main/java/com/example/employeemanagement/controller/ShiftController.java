package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.service.ManagerService;
import com.example.employeemanagement.service.ShiftService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/shift")
@AllArgsConstructor
@Log4j2
public class ShiftController {
    private final ShiftService shiftService;

    private final ManagerService managerService;

    @GetMapping("/test")
    public String test() {
        return managerService.testEvaluate();
    }

    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }

    @GetMapping("/preweek")
    public List<Shift> getAllFromPreWeek() {
        return shiftService.getShiftsPostedInLatestWeek();
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
            managerService.deleteShiftById(id);
            return new ResponseEntity<>("Successfully deleted shift with id: " + id, HttpStatus.OK);
        } catch (Exception e) {
            // Catch any exceptions and return an error response
            String errorMessage = "Error occurred while deleting the shift: " + e.getMessage();
            // Return the error message along with the 500 INTERNAL_SERVER_ERROR status
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // This method allows only users with 'EMPLOYEE' authority to add themselves to a shift
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/{shiftId}/addEmployee")
    public ResponseEntity<String> addEmployeeToShift(@PathVariable Long shiftId) {
        try {
            shiftService.addEmployeeToShift(shiftId);
            return new ResponseEntity<>("Employee added to the shift.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // User or shift not found
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Shift is full or cannot add employee
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
