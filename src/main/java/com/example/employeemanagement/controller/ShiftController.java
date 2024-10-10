package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.service.ShiftService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/shift")
@Log4j2
public class ShiftController {
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
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
    public ResponseEntity<?> createShift(@RequestBody ShiftDTO shift) {

        try {
            // Attempt to save the shift
            Shift savedShift = shiftService.saveShift(shift);
            // Return the saved shift along with the 201 CREATED status
            return new ResponseEntity<>(savedShift, HttpStatus.CREATED);
        } catch (Exception e) {
            // Catch any exceptions and return an error response
            String errorMessage = "Error occurred while creating the shift: " + e.getMessage();
            // Return the error message along with the 500 INTERNAL_SERVER_ERROR status
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
    }

    // This method allows only users with 'EMPLOYEE' authority to add themselves to a shift
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PostMapping("/{shiftId}/addEmployee")
    public ResponseEntity<String> addEmployeeToShift(@PathVariable Long shiftId) {
        String result = shiftService.addEmployeeToShift(shiftId);
        if (result.contains("Error") || result.contains("full")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
