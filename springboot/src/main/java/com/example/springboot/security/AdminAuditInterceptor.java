package com.example.springboot.security;

import com.example.springboot.domain.AdminAuditLog;
import com.example.springboot.repository.AdminAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuditInterceptor implements HandlerInterceptor {

    private final AdminAuditLogRepository auditLogRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuditInterceptor(AdminAuditLogRepository auditLogRepository, JwtTokenProvider jwtTokenProvider) {
        this.auditLogRepository = auditLogRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin")) {
            AdminAuditLog log = new AdminAuditLog();
            log.setApiUrl(uri);
            log.setActionType(request.getMethod());
            log.setIpAddress(request.getRemoteAddr());
            log.setParameters(request.getQueryString());
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    Long adminId = jwtTokenProvider.extractUserId(token);
                    log.setAdminId(adminId);
                } catch (Exception ignored) {}
            }
            
            auditLogRepository.save(log);
        }
    }
}
