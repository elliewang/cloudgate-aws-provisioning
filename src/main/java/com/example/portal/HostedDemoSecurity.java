package com.example.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** A shared team-demo login, NOT enterprise identity/role management. */
@Configuration
@Profile("render")
public class HostedDemoSecurity {
  @Bean
  SecurityFilterChain hostedDemoFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable()) // Stateless demo APIs; not suitable for production web sessions
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/healthz").permitAll()
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  @Bean
  UserDetailsService demoUsers(
      @Value("${DEMO_LOGIN_USER}") String name,
      @Value("${DEMO_LOGIN_PASSWORD}") String password) {
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return new InMemoryUserDetailsManager(
        User.withUsername(name).password(encoder.encode(password)).roles("DEMO").build());
  }
}
