package com.example.employeemanagement.redis;

import lombok.Data;
import java.io.Serializable;

@Data
public class AddressCache implements Serializable {
    private static final long serialVersionUID = 1L;  // Ensures compatibility for Redis serialization

    private Long id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;

    public AddressCache(Long id, String street, String city, String state, String postalCode, String country, Double latitude, Double longitude) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
