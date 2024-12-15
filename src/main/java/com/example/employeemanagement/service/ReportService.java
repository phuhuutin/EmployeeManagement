package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.ReportDTO;
import com.example.employeemanagement.entity.Report;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ShiftService shiftService;
    private final AttendanceRecordService attendanceRecordService;
    private final ClockInAndOutService clockInAndOutService;
    private final UserService userService;

    public Report closedReport(Long reportId){
        Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));
        report.setCompleted(true);
        return reportRepository.save(report);
    }
    public List<Report> getAllReports(){
        User user = userService.getCurrentUser();
        return reportRepository.findAllCompletedReportsByEmployerId(user.getEmployer().getId());
    }

    public Report createReport(ReportDTO reportDTO) throws DataIntegrityViolationException {
        Report report = new Report();
        report.setReportDate(LocalDate.now());
        report.setCompleted(false);
        report.setDetails(reportDTO.getDetails());
        report.setType(reportDTO.getType());
        report.setUser(userService.getCurrentUser());
        Shift shift = shiftService.findShiftById(reportDTO.getShiftId());
        report.setShift(shift);
        if(reportDTO.getType().equals(Report.ReportType.CLOCK)){
            report.setClockInOutRecord(clockInAndOutService.findById(reportDTO.getClockInOutRecordId()));
        }
        return reportRepository.save(report);
    }

    public List<Report> getReportsByUserAndType(User user, Report.ReportType type) {
        return reportRepository.findByUserAndType(user, type);
    }

    public List<Report> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByReportDateBetween(startDate, endDate);
    }
}