package com.sldlt.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
                .antMatchers("/js/**")
                .antMatchers("/css/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
                .disable()
            .headers()
                .referrerPolicy(ReferrerPolicy.SAME_ORIGIN)
                    .and()
                .and()
            .authorizeRequests()
                .antMatchers("/api").permitAll()
                .antMatchers("/**").permitAll();
    }

}
