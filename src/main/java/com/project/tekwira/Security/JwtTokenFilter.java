package com.project.tekwira.Security;

import com.project.tekwira.Models.Role;
import com.project.tekwira.Models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // No token? Continue without setting auth
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            if (!jwtUtil.validateAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] subject = jwtUtil.getSubject(token).split(",");
            Long userId = Long.parseLong(subject[0]);
            String role = jwtUtil.getRole(token);
            String phoneNumber = jwtUtil.getPhoneNumber(token);
            String firstName = jwtUtil.getFirstName(token);
            String lastName = jwtUtil.getLastName(token);
            String email = jwtUtil.getEmail(token);
            String username = jwtUtil.getUsername(token);

            User userDetails = User.builder()
                    .userId(userId)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .role(Role.valueOf(role))
                    .firstName((firstName))
                    .lastName((lastName))
                    .email(email)
                    .username(username)
                    .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            // Do NOT throw or return unauthorized here — just continue without authentication
            SecurityContextHolder.clearContext(); // Optional: clean any partial auth
        }

        // Always proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}
