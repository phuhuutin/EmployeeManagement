package com.example.employeemanagement.redis.repository;

import com.example.employeemanagement.redis.FindShiftCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FindShiftCacheRepository extends CrudRepository<FindShiftCache, Long> {
}