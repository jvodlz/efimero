package com.example.efimero.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // DEV ONLY. H2 console needs JavaScript - skip security headers
        if (path.startsWith("/h2-console")) {
            chain.doFilter(req, res);
            return;
        }

        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("Permissions-Policy", "microphone=(), camera=()");
        res.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        chain.doFilter(req, res);
    }
}
