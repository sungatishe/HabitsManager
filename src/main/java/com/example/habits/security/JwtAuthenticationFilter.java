package com.example.habits.security;

import com.example.habits.service.JwtService;
import com.example.habits.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("Token to validate in JwtAuthenticationFilter: " + token);

            try {
                if (jwtService.isTokenValid(token)) {
                    System.out.println("Token is valid according to JwtService");
                    if (!jwtService.isTokenExpired(token)) {
                        System.out.println("Token is not expired");
                        Claims claims = jwtService.extractClaims(token);
                        System.out.println("Token validated successfully in JwtAuthenticationFilter. Claims: " + claims);
                        String username = claims.getSubject();

                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            UserDetails userDetails = userService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            System.out.println("Authentication set for user: " + username);
                        }
                    } else {
                        System.err.println("Token is expired in JwtAuthenticationFilter");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                        return;
                    }
                } else {
                    System.err.println("Token is invalid in JwtAuthenticationFilter");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Exception during token validation in JwtAuthenticationFilter: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("No Bearer token found in Authorization header");
        }

        filterChain.doFilter(request, response);
    }
}