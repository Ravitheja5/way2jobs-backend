package com.way2jobs.security;

import com.way2jobs.entity.User;
import com.way2jobs.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.Collections;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final UserRepository userRepository;

        public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
                this.jwtUtil = jwtUtil;
                this.userRepository = userRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                                                        HttpServletResponse response,
                                                                        FilterChain filterChain)
                        throws ServletException, IOException {

                String header = request.getHeader("Authorization");

                if (header != null && header.startsWith("Bearer ")) {

                        String token = header.substring(7);

                        if (jwtUtil.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {

                                String email = jwtUtil.extractEmail(token);

                                User user = userRepository.findByEmail(email).orElse(null);
                                if (user != null) {
                                        authenticate(user, user.getRole(), request);
                                } else if ("ADMIN".equals(jwtUtil.extractRole(token))) {
                                        authenticate(email, "ADMIN", request);
                                }

                        }

                }

                filterChain.doFilter(request, response);
        }
        private void authenticate(Object principal, String role, HttpServletRequest request) {
                String normalizedRole = role == null ? "USER" : role;
                String authority = normalizedRole.startsWith("ROLE_")
                                ? normalizedRole
                                : "ROLE_" + normalizedRole;
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority(authority)
                );
                UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

}