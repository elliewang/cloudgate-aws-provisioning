package com.example.portal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Keep local and automated-test behavior unchanged from the original starter. */
@Configuration
@Profile("!render")
public class LocalDemoSecurity {
  @Bean SecurityFilterChain localDemoFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
  }
}
