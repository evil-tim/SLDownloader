package com.sldlt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http //
            .csrf(csrf -> csrf //
                .disable()) //
            .headers(headers -> headers //
                .referrerPolicy(referrerPolicy -> referrerPolicy //
                    .policy(ReferrerPolicy.SAME_ORIGIN))) //
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests //
                // static files
                .requestMatchers("/js/**").permitAll() //
                .requestMatchers("/css/**").permitAll() //
                .requestMatchers("/webjars/**").permitAll() //
                // API
                .requestMatchers("/api").permitAll() //
                // catch all
                .requestMatchers("/**").permitAll());
        return http.build();
    }

}
