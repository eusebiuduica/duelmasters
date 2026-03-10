package org.example.duelmasters.Configurations;

import org.example.duelmasters.Filters.JwtFilter;
import org.example.duelmasters.Handlers.AuthExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthExceptionHandler authExceptionHandler;
    private final String allowedOrigins;

    public SecurityConfig(JwtFilter jwtFilter,
                          AccessDeniedHandler accessDeniedHandler,
                          AuthExceptionHandler authExceptionHandler,
                          @Value("${cors.allowed-origins}") String allowedOrigins) {
        this.jwtFilter = jwtFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authExceptionHandler = authExceptionHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/marketplace/stream").permitAll() // allow SSE stream
                        .requestMatchers("/register").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authExceptionHandler)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    //CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // cookie
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
