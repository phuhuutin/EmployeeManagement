package com.example.employeemanagement.security;

import com.example.employeemanagement.controller.UserController;
import com.example.employeemanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        User user = (User) userDetailsService.loadUserByUsername(username);
        logger.info(user.getUsername() + " " + user.getPassword() + "  " + user.getRole());
        // Replace with your password verification logic
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("Matches");
            return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
        } else {
            throw new AuthenticationException("Authentication failed") {};
        }
    }
}
