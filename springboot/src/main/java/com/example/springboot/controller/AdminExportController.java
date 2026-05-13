package com.example.springboot.controller;

import com.example.springboot.service.AdminExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/export")
public class AdminExportController {

    private final AdminExportService exportService;

    public AdminExportController(AdminExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/reservations/csv")
    public ResponseEntity<byte[]> exportReservationsCsv(
            @RequestHeader(value = "Authorization") String authorization
    ) {
        String csvData = exportService.exportReservationsToCsv(authorization);
        byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reservations_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
