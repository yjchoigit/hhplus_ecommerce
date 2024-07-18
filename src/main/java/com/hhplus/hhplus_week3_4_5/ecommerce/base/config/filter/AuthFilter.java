package com.hhplus.hhplus_week3_4_5.ecommerce.base.config.filter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

@Slf4j
@WebFilter("/*")
@Component
public class AuthFilter implements Filter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.info("---- AuthFilter >>> doFilter ----");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if authentication token (JWT) exists in request header
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing or invalid Authorization header");
            log.error("Missing or invalid Authorization header");
            return;
        }

        // Extract JWT token from Authorization header
        String jwt = authHeader.substring(7); // "Bearer " 다음의 토큰 문자열 추출

        try {
            if (!isValidToken(jwt)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Invalid JWT token");
                log.error("Invalid JWT token");
                return;
            }
        } catch (ExpiredJwtException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Expired JWT token");
            log.error("Expired JWT token");
            return;
        } catch (SignatureException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Invalid JWT signature");
            log.error("Invalid JWT signature");
            return;
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("JWT token validation error");
            log.error("JWT token validation error");
            return;
        }

        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    private boolean isValidToken(String jwt) {
        log.info("---- AuthFilter >>> isValidToken ----");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return !(claims == null || claims.getExpiration().before(new Date()));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("---- AuthFilter >>> init ----");
    }

    @Override
    public void destroy() {
        log.info("---- AuthFilter >>> destroy ----");
    }
}