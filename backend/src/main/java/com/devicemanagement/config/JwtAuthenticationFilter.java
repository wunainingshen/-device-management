package com.devicemanagement.config;

import com.devicemanagement.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        log.debug("=== JWT Filter: {} {} | AuthHeader: {}", request.getMethod(), request.getRequestURI(), authHeader != null ? "Bearer " + authHeader.substring(7).substring(0, Math.min(20, authHeader.length()-7)) + "..." : "NONE");

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            log.debug("=== JWT Filter: Token extracted ({} chars), validating...", token.length());
            boolean valid = jwtUtils.validateToken(token);
            log.debug("=== JWT Filter: Token validation result: {}", valid);
            if (valid) {
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                Long userId = jwtUtils.getUserIdFromToken(token);
                log.debug("=== JWT Filter: Auth set for user={}, role={}", username, role);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, userId,
                                Collections.singletonList(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            log.debug("=== JWT Filter: No token found in request");
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
