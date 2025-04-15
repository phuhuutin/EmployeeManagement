package com.example.employeemanagement.redis.service;

import com.example.employeemanagement.entity.Employer;
import com.example.employeemanagement.redis.AddressCache;
import com.example.employeemanagement.redis.EmployerCache;
import com.example.employeemanagement.redis.repository.EmployerCacheRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
@AllArgsConstructor
@Service
public class EmployerCacheService {

     private EmployerCacheRepository employerCacheRepository;
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * Save or update employer in Redis cache.
     */

    public void save(EmployerCache employerCache){
        employerCacheRepository.save(employerCache);
    }
    public void saveEmployerToCache(Employer employer) {
        if (employer == null) return;

        // Convert Employer entity to EmployerCache DTO
        EmployerCache employerCache = new EmployerCache(
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

        // Save to Redis
        employerCacheRepository.save(employerCache);
        System.out.println("✅ Employer cached: " + employer.getId());
    }

    /**
     * Retrieve employer from Redis cache.
     */
    public Optional<EmployerCache> getEmployerFromCache(Long employerId) {
        return employerCacheRepository.findById(employerId);
    }
}
