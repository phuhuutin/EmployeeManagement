package com.example.employeemanagement.redis;

import com.example.employeemanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@RedisHash( value =  "user_cache", timeToLive = 1800)
public class UserCache implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String CACHE_NAME = "user_cache";
    public static final Integer CACHE_TTL = 1800; //in seconds
    @Id
    private Long id;
    private String username;
    private String email;
    private String role;

    public static UserCache fromUser(User user) {
        return new UserCache(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setRole(this.role);
        return user;
    }
}