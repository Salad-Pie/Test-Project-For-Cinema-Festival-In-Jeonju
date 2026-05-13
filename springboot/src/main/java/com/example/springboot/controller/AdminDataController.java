package com.example.springboot.controller;

import com.example.springboot.service.AdminDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
public class AdminDataController {

    private final AdminDataService service;

    @GetMapping("/{entityType}")
    public ResponseEntity<List<?>> getData(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String entityType
    ) {
        return ResponseEntity.ok(service.getData(authorization, entityType));
    }
}
