package com.niloy.student_portal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    // API Security Filter Chain - for REST API with HTTP Basic and session support
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()

                // Department endpoints
                .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()

                // Course endpoints
                .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()

                // Student endpoints
                .requestMatchers(HttpMethod.POST, "/api/students/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/api/students/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/students/**").authenticated()

                // Enrollment endpoints - Students only
                .requestMatchers("/api/enrollment/**").hasRole("STUDENT")

                // Teacher endpoints
                .requestMatchers("/api/teachers/**").hasRole("TEACHER")

                // Profile endpoints
                .requestMatchers("/api/profile/**").authenticated()

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Web Security Filter Chain - for web pages with form login
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/web/enrollment/**").hasRole("STUDENT")
                .requestMatchers("/web/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/web/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
