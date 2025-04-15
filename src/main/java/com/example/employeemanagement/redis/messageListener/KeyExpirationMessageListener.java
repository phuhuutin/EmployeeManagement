package com.example.employeemanagement.redis.messageListener;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class KeyExpirationMessageListener {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public KeyExpirationMessageListener(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void onMessage(String message) {
        // The message contains the key that has expired (the hash key).
        System.out.println("Key expired: " + message);

            // Extract the user ID from the expired key (e.g., "18" from "user_cache:18")
            String id = message.split(":")[1];

            // The set key that contains the user ID (e.g., "user_cache")
            String setKey = message.split(":")[0];;
            // Validate that the ID is numeric
            if (!id.matches("\\d+")) { // Ensures ID consists only of digits
                System.err.println("Invalid ID format (must be numeric): " + id);
                return; // Ignore invalid IDs
            }
            // Remove the expired user ID from the set (e.g., remove "18" from "user_cache")
            redisTemplate.opsForSet().remove(setKey, id);

            System.out.println("Removed user ID " + id + " from set: " + setKey);
       // }
    }
}
