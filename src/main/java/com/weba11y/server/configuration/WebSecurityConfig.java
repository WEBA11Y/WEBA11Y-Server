package com.weba11y.server.configuration;

import com.weba11y.server.filter.JwtFilter;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${client.url}")
    private String clientUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(httpBasic -> httpBasic.disable()) // httpBasic 기본 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(csrf -> csrf.disable()) // csrf 기본 설정
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/api/v1/accessibility/**").access(hasRole("USER"))
                        .requestMatchers("/api/v1/member/**").hasRole("USER")
                        .requestMatchers("/api/v1/urls/**").hasRole("USER")
                        .requestMatchers("/api/v1/violations/**").hasRole("USER")
                        .requestMatchers("/api/v1/inspection-summaries/**").hasRole("USER")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtFilter(secret), UsernamePasswordAuthenticationFilter.class) // Filter 동작 이전에 JWT Filter동작
                .build();
    }

    // CORS 설정 ( Cross Origin Resource Sharing )
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of(clientUrl)); // Client URL
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 허용할 요청 HTTP Method
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 비밀번호 해싱을 위한 Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

