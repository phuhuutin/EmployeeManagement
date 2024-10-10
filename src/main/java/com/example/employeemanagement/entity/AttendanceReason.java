package com.example.employeemanagement.entity;


public enum AttendanceReason {
    LATE(-1, "Late"),           // 1 point deduction for being late
    ABSENT(-2, "Absent"),       // 2 points deduction for being absent
    LEAVEEARLY(-1, "Leave Early");      // 0 points for being on time

    private final int points;
    private final String name;


    AttendanceReason(int points, String name) {
        this.points = points;
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }
}
