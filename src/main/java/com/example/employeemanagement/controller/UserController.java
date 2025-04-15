package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.entity.AttendanceRecord;
import com.example.employeemanagement.entity.Payroll;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.redis.UserCache;
import com.example.employeemanagement.redis.service.UserCacheService;
import com.example.employeemanagement.repository.AttendancePointsRepository;
import com.example.employeemanagement.security.AppUserDetailService;
import com.example.employeemanagement.security.CustomAuthenticationManager;
import com.example.employeemanagement.service.AttendanceRecordService;
import com.example.employeemanagement.service.EmployerService;
import com.example.employeemanagement.service.PayrollService;
import com.example.employeemanagement.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080/")
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final CustomAuthenticationManager customAuthenticationManager;
    private final PayrollService payrollService;
    private final AppUserDetailService appUserDetailService;
    private final UserService userService;
    private final UserCacheService userCacheService;
    @Autowired
    private final AttendanceRecordService attendanceRecordService;
    private final EmployerService employerService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try{
            User user = userService.getUserById(id);
            userCacheService.save(user, UserCache.CACHE_TTL);
            return ResponseEntity.ok(user);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try{
            User user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @GetMapping("/payrolls")
    public ResponseEntity<List<Payroll>> getUserPayrolls() {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(payrollService.findPayrollsbyUserId(user.getId()));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

//    @DeleteMapping("/{id}")
//    public void deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//    }
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            User user = userService.signUpUser(signUpRequest, "EMPLOYEE");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody UserLoginDTO loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = customAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Load the user details
            UserDetails userDetails = appUserDetailService.loadUserByUsername(loginRequest.getUsername());

            // Return the User information (excluding password)
            User user = userService.getUserById(((User) userDetails).getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null); // Handle authentication failure
        }
    }
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordsChangeRequest re) {
        try {
            return ResponseEntity.ok(userService.changePasswords(re));
        } catch (Exception e) {
             return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceRecord>> getListOfAttandanceForCurrentUser(){
        try{
            User user = userService.getCurrentUser();
            return new ResponseEntity<>(user.getAttendancePoints().getAttendanceRecords(), HttpStatus.OK);
        }catch (AccessDeniedException exception){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }catch (Exception exception){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestBody InitialSetupDTO dto){
        try{
            User user = employerService.initialSetup(dto);
            return ResponseEntity.ok(user);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            boolean isDeleted = userService.deleteUser(id);
            if (isDeleted) {
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    @PutMapping("/payrate")
    public ResponseEntity<String> updateUserPayRate(@RequestBody PayRateUpdateRequest payRateUpdateRequest) {
        try {
            User updatedUser = userService.updateUserPayRate(payRateUpdateRequest);
            if (updatedUser != null) {
                return ResponseEntity.ok("new pay rate is updated!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + payRateUpdateRequest.getUserId());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating pay rate: " + e.getMessage());
        }
    }

    }
