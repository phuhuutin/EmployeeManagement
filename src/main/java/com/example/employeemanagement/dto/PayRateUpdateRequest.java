package com.example.employeemanagement.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PayRateUpdateRequest {
    private double payRate;
    private Long userId;
}
