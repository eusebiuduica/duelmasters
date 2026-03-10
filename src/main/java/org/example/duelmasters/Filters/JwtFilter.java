package org.example.duelmasters.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.duelmasters.Models.User;
import org.example.duelmasters.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");

        if (null != bearerToken && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            User user = userService.validateUser(token);

            if (null != user) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getId(), null,null));
            }
        }

        filterChain.doFilter(request, response);
    }
}
