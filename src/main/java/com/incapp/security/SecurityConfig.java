package com.incapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	BCryptPasswordEncoder getpasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .anyRequest().permitAll() // ✅ Permit ALL URLs
	        )
            // Google login
            .oauth2Login(oauth -> oauth
                .loginPage("/login-signup")
                .defaultSuccessUrl("/user/oauth2success", true)
            )
            // No Spring-managed form login!
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login-signup")
                .permitAll()
            );

        return http.build();
    }
}
