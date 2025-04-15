package com.example.employeemanagement.redis;

import com.example.employeemanagement.entity.Employer;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@RedisHash(value = "employer", timeToLive = 1800)  // Stores in Redis under the "employer" keyspace)
public class EmployerCache implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String name;
    private AddressCache address;

    public EmployerCache(Long id, String name, AddressCache address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    public EmployerCache fromEmployer(Employer employer) {
        return new EmployerCache(
                employer.getId() ,
                employer.getName(),
                new AddressCache(
                        employer.getAddress().getId(),
                        employer.getAddress().getStreet(),
                        employer.getAddress().getCity(),
                        employer.getAddress().getState(),
                        employer.getAddress().getPostalCode(),
                        employer.getAddress().getCountry(),
                        employer.getAddress().getLatitude(),
                        employer.getAddress().getLongitude()
                )
        );
    }
}