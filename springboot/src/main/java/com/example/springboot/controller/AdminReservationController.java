package com.example.springboot.controller;

import com.example.springboot.dto.AdminReservationResponse;
import com.example.springboot.dto.AdminReservationTypeResponse;
import com.example.springboot.service.AdminReservationService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService service;

    public AdminReservationController(AdminReservationService service) {
        this.service = service;
    }

    @GetMapping("/types")
    public ResponseEntity<List<AdminReservationTypeResponse>> types(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ResponseEntity.ok(service.types(authorization));
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationResponse>> reservations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(required = false) String projectKey
    ) {
        return ResponseEntity.ok(service.reservations(authorization, type, date, time, projectKey));
    }
}
