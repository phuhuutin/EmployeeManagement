package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.InitialSetupDTO;
import com.example.employeemanagement.dto.SignUpRequest;
import com.example.employeemanagement.entity.Address;
import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.AddressRepository;
import com.example.employeemanagement.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository employerRepository;
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(EmployerService.class);


    @Transactional
    public User initialSetup(InitialSetupDTO setupDTO){
        logger.error(setupDTO.toString());
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
        String employerName = setupDTO.getEmployerName();

        if (employerName == null || employerName.isEmpty()) {
            throw new IllegalArgumentException("Employer name must not be null or empty");
        }
        employer.setName(employerName);
        employer.setAddress(address);
        employer = employerRepository.save(employer);

        // Create and save manager user
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmployerId(employer.getId());
        signUpRequest.setEmail(setupDTO.getEmail());
        signUpRequest.setUsername(setupDTO.getUsername());
        signUpRequest.setPassword(setupDTO.getPassword());

        return userService.signUpUser(signUpRequest, "MANAGER");
    }
}
