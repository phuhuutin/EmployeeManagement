package com.example.employeemanagement.dto;

import org.hibernate.annotations.processing.Pattern;
import lombok.Data;
@Data
public class AddressDto {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
}