package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.SignUpRequest;
import com.example.employeemanagement.entity.AttendancePoints;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.AttendancePointsRepository;
import com.example.employeemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private AttendancePointsRepository attendancePointsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User signUpUser(SignUpRequest signUpRequest) {
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
        user.setRole(signUpRequest.getRole());

        user.setAttendancePoints(new AttendancePoints(user));

        // Save the new user in the repository
        return userRepository.save(user);
    }

}
