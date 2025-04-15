package com.example.employeemanagement.redis.repository;
import com.example.employeemanagement.redis.EmployerCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployerCacheRepository extends CrudRepository<EmployerCache, Long> {
}