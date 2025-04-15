package com.example.employeemanagement.redis.service;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.redis.UserCache;
import com.example.employeemanagement.redis.repository.UserCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserCacheService {

    @Autowired
    private UserCacheRepository userCacheRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UserCacheService.class);



    /**
     * ✅ Save user details to Redis cache.
     */
    public void save(User user, int minutes) {
        if (user == null) return;
        userCacheRepository.save(user.toUserCache());
        logger.info("✅ Cached user: " + user.getId());
    }
    /**
     * ✅ Save user details to Redis cache.
     */
    public void save(UserCache userCache, int minutes) {
        if (userCache == null) return;

        // Convert User to UserCache
        // Define the key (e.g., "usercache:<id>")
        String key = UserCache.CACHE_NAME + userCache.getId();

        // Use HashOperations to store the UserCache as a Redis Hash
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, "data", userCache);

        // Set expiration
        redisTemplate.expire(key, Duration.ofMinutes(minutes));

        logger.info("✅ Cached user: " + userCache.getId());
    }

    /**
     * ✅ Retrieve user from cache.
     */
    public Optional<UserCache> findById(Long userId) {
        return userCacheRepository.findById(userId);
    }

    /**
     * ✅ Remove user from cache.
     */
    public void removeById(Long userId) {
        userCacheRepository.deleteById(userId);
        System.out.println("🗑 Removed user from cache: " + userId);
    }
}
