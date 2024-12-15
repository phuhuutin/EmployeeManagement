package com.example.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitialSetupDTO {
    // Employer details
    private String employerName;

    // Address details
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;

    // Manager details
    private String username;
    private String password;
    private String email;

    @Override
    public String toString() {
        return "InitialSetupDTO{" +
                "employerName='" + employerName + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +  // Mask sensitive information like passwords
                ", email='" + email + '\'' +
                '}';
    }
}