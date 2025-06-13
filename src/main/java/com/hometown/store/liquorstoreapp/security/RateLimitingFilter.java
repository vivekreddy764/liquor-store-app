package com.hometown.store.liquorstoreapp.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Different rate limits for different endpoints
    private final Map<String, Bandwidth> endpointLimits = Map.of(
        "/api/auth/login", Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))), // 5 login attempts per minute
        "/api/auth/signup", Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))), // 3 signups per minute
        "/api/products", Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))), // 100 product requests per minute
        "default", Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1))) // 50 requests per minute for other endpoints
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getRequestURI();
        String key = clientIp + ":" + endpoint;
        
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(endpoint));
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\",\"code\":429}");
        }
    }
    
    private Bucket createBucket(String endpoint) {
        Bandwidth limit = endpointLimits.entrySet().stream()
            .filter(entry -> endpoint.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(endpointLimits.get("default"));
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}