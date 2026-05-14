package com.example.springboot.controller;

import com.example.springboot.domain.User;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String authorization) {
        requireAdmin(authorization);
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        requireAdmin(authorization);
        
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("USER") && !newRole.equals("ADMIN"))) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setRole(newRole);
        return ResponseEntity.ok(userRepository.save(user));
    }

    private void requireAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Admin authorization is required");
        }
        String token = authorization.substring(7);
        Long requesterId = jwtTokenProvider.extractUserId(token);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        
        if (!"ADMIN".equals(requester.getRole())) {
            throw new SecurityException("Admin role is required");
        }
    }
}
