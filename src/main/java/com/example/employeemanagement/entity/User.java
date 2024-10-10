package com.example.employeemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String username;
    @Column( length = 100, nullable = false)
    @JsonIgnore
    private String password;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    private double payRate;  // Pay rate for the employee


    // Role can be "MANAGER" or "EMPLOYEE"
    private String role;

    // Employees can pick multiple shifts
    @ManyToMany
    @JoinTable(
            name = "user_shifts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    @JsonIgnore
    private List<Shift> pickedShifts;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attendance_points_id")
    private AttendancePoints attendancePoints ;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> this.role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", payRate=" + payRate +
                ", role='" + role + '\'' +
                ", pickedShifts=" + (pickedShifts != null ? pickedShifts.size() : 0) +  // Avoid printing the whole list, just show the count
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User otherUser = (User)obj;
        return this.getId().equals(otherUser.getId());
    }
}
