package com.example.employeemanagement.redis.repository;

import com.example.employeemanagement.redis.UserCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCacheRepository extends CrudRepository<UserCache, Long> {
    Optional<UserCache> findByUsername(Long userId);
}