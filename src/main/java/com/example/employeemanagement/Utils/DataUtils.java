package com.example.employeemanagement.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DataUtils {
    // Get the start of the previous week (Sunday)
    public static LocalDate getStartOfPreviousWeek() {
        LocalDate currentDate = LocalDate.now();
        // Go back one week, then get the Sunday of that week
        return currentDate.minusWeeks(2).with(DayOfWeek.SUNDAY);
    }

    // Get the end of the previous week (Saturday)
    public static LocalDate getEndOfPreviousWeek() {
        LocalDate currentDate = LocalDate.now();

        // Go back one week, then get the Saturday of that week
        return currentDate.minusWeeks(1).with(DayOfWeek.SATURDAY);
    }

}
