This backend server is designed to work with this android repo: https://github.com/phuhuutin/AtoB
This project provides services for managing employees across multiple employers, including functionalities for handling shifts, clock-in/clock-out records, attendance, payroll, and reporting (e.g., tracking clock-in/out times).

Main User Story: A manager can create a shift, and the system automatically schedules a background job to evaluate the shift's payroll and attendance records (e.g., Late, Leave Early, Absent). Attendance records expire 30 days after the shift ends, and evaluations occur 24 hours after the shift concludes.

Employees can pick shifts and clock in/out via services, which are accessed through a client-side application, such as an Android app.
