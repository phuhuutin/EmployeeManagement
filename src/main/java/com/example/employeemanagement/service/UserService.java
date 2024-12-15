package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.PasswordsChangeRequest;
import com.example.employeemanagement.dto.PayRateUpdateRequest;
import com.example.employeemanagement.dto.SignUpRequest;
import com.example.employeemanagement.entity.AttendancePoints;
import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.AttendancePointsRepository;
import com.example.employeemanagement.repository.EmployerRepository;
import com.example.employeemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AttendancePointsRepository attendancePointsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployerRepository employerRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Manually handle AttendancePoints
        AttendancePoints attendancePoints = user.getAttendancePoints();
        if (attendancePoints != null) {
            attendancePoints.setUser(null); // Break the circular reference
            attendancePointsRepository.delete(attendancePoints);
        }

        // Remove the user
        userRepository.delete(user);
        return true;
    }

    public User signUpUser(SignUpRequest signUpRequest, String role) {
        Optional<Employer> employerOptional = employerRepository.findById(signUpRequest.getEmployerId());
        Employer employer;
        if (employerOptional.isPresent()) {
            employer = employerOptional.get();
        } else {
            throw new IllegalArgumentException("Can not find the given employer");
        }
        // Check if the username or email is already taken
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken");
        }
        // Create a new user entity from the sign-up request
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword())); // Encode password
        user.setEmail(signUpRequest.getEmail());
        user.setRole(role);
        user.setEmployer(employer);
        employer.getEmployees().add(user);
        user.setAttendancePoints(new AttendancePoints(user));
        return userRepository.save(user);
    }

    public User getCurrentUser(){
        // Get the currently authenticated user from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user

        // Fetch the user (employee) from the repository using the username
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found with username: " + username));
    }

    public User changePasswords(PasswordsChangeRequest re){
        if(re.getOldPassword().equals(re.getNewPassword())){
            throw new IllegalArgumentException("The old and new passwords can not be the same.");
        }

        User currentUser = getCurrentUser();
        if(!passwordEncoder.matches(re.getOldPassword(), currentUser.getPassword())){
             throw new IllegalArgumentException("Old password does not match.");
        }

        currentUser.setPassword(passwordEncoder.encode(re.getNewPassword()));
        return userRepository.save(currentUser);

    }

    @Transactional
    public User updateUserPayRate(PayRateUpdateRequest request) {
        // Find the user by ID
        User user = userRepository.findById(request.getUserId()).orElse(null);

        // If the user exists, update the pay rate and save it
        if (user != null) {
            user.setPayRate(request.getPayRate());
            return userRepository.save(user);  // Save the updated user
        }

        return null;  // Return null if the user is not found
    }

}
