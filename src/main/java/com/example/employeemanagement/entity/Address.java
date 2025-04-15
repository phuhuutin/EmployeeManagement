package com.example.employeemanagement.entity;

import com.example.employeemanagement.redis.AddressCache;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nullable
    private String street;
    @Nullable
    private String city;
    @Nullable
    private String state;
    @Nullable
    private String postalCode;
    @Nullable
    private String country;
    @Nullable
    private Double latitude;
    @Nullable
    private Double longitude;

    public AddressCache MaptoAddressCache() {
        return new AddressCache(this.id, this.street, this.city, this.state, this.postalCode, this.country, this.latitude, this.longitude);
    }
}