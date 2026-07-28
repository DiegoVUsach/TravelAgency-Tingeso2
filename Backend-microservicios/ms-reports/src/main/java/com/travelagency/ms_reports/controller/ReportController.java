package com.travelagency.ms_reports.controller;

import com.travelagency.ms_reports.dto.PackageRankingDTO;
import com.travelagency.ms_reports.dto.ReservationDTO;
import com.travelagency.ms_reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Direct /api/v1/reports/sales
    @GetMapping("/api/v1/reports/sales")
    public ResponseEntity<List<ReservationDTO>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ReservationDTO> sales = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    // Legacy route /api/v1/reservations/reports/sales routed to ms-reports by Gateway
    @GetMapping("/api/v1/reservations/reports/sales")
    public ResponseEntity<List<ReservationDTO>> getSalesReportLegacy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ReservationDTO> sales = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    // Direct /api/v1/reports/ranking
    @GetMapping("/api/v1/reports/ranking")
    public ResponseEntity<List<PackageRankingDTO>> getPackageRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PackageRankingDTO> ranking = reportService.getPackageRanking(startDate, endDate);
        return ResponseEntity.ok(ranking);
    }

    // Legacy route /api/v1/reservations/reports/ranking routed to ms-reports by Gateway
    @GetMapping("/api/v1/reservations/reports/ranking")
    public ResponseEntity<List<PackageRankingDTO>> getPackageRankingLegacy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PackageRankingDTO> ranking = reportService.getPackageRanking(startDate, endDate);
        return ResponseEntity.ok(ranking);
    }
}
