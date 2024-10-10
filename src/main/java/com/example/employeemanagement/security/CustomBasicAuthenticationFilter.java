package com.example.employeemanagement.security;

import com.example.employeemanagement.controller.UserController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.Base64;

public class CustomBasicAuthenticationFilter extends BasicAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    public CustomBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws java.io.IOException, ServletException {
        String header = request.getHeader("Authorization");



        if (header != null && header.startsWith("Basic ")) {
            String credentials = header.substring("Basic ".length()).trim();
            String decodedCredentials = new String(Base64.getDecoder().decode(credentials));
            String[] userPass = decodedCredentials.split(":", 2);

            String username = userPass[0];
            String password = userPass.length > 1 ? userPass[1] : "";
            // Create an authentication token
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            try {
                // Authenticate the user
                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException e) {
                // Handle authentication failure
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                return; // Stop further processing
            }
        }

        chain.doFilter(request, response); // Continue with the filter chain
    }
}
