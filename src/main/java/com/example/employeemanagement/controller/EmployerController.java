package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.InitialSetupDTO;
import com.example.employeemanagement.dto.SignUpRequest;
import com.example.employeemanagement.entity.Address;
import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.AddressRepository;
import com.example.employeemanagement.repository.EmployerRepository;
import com.example.employeemanagement.service.AttendanceRecordService;
import com.example.employeemanagement.service.UserService;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/employer")
@AllArgsConstructor
public class EmployerController {
     private final EmployerRepository employerRepository;
     private final AddressRepository addressRepository;
     private final UserService userService;
     private final AttendanceRecordService attendanceRecordService;

    @PostMapping("/setup")
    public ResponseEntity<?> initialSetup(@RequestBody InitialSetupDTO setupDTO) {
        try {
            // Create and save address
            Address address = new Address();
            address.setStreet(setupDTO.getStreet());
            address.setCity(setupDTO.getCity());
            address.setState(setupDTO.getState());
            address.setPostalCode(setupDTO.getPostalCode());
            address.setCountry(setupDTO.getCountry());
            address = addressRepository.save(address);

            // Create and save employer
            Employer employer = new Employer();
            employer.setName(setupDTO.getEmployerName());
            employer.setAddress(address);
            employer = employerRepository.save(employer);

            // Create and save manager user
            SignUpRequest signUpRequest = new SignUpRequest();
            signUpRequest.setEmployerId(employer.getId());
            signUpRequest.setEmail(setupDTO.getEmail());
            signUpRequest.setUsername(setupDTO.getUsername());
            signUpRequest.setPassword(setupDTO.getPassword());

            User manager = userService.signUpUser(signUpRequest, "MANAGER");

            return ResponseEntity.status(HttpStatus.CREATED).body(manager);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during setup: " + e.getMessage());
        }
    }
    @GetMapping
    public List<Employer> getAll(){
        return employerRepository.findAll();
    }

    @GetMapping("/check")
    public boolean checkInit(){
        List<Employer> employerList = employerRepository.findAll();
        return !employerList.isEmpty();
    }

    @DeleteMapping("/deleteRecord/{id}")
    private ResponseEntity<String> deleteAttendanceRecordById(@PathVariable Long id) {
        try {
            // Fetch the record by ID
            AttendanceRecord record = attendanceRecordService.findById(id);
            if (record == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record not found");
            }
            attendanceRecordService.delete(record);
            // If the record has a job ID, delete the background job
            if (record.getJobId() != null) {
                BackgroundJob.delete(record.getJobId());
            }
            return ResponseEntity.ok("The attendance record is deleted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting record: " + e.getMessage());
        }


    }

}
