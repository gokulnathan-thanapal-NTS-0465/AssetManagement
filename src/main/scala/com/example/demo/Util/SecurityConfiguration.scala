package com.example.demo.Util

import org.springframework.security.config.Customizer
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity(prePostEnabled = true) 
class SecurityConfiguration (jwtAuthenticationFilter: JwtAuthenticationFilter) {
  @Bean
  def passwordEncoder(): PasswordEncoder={
      new BCryptPasswordEncoder()
  }
  @Bean
  def securityFilterChain(http: HttpSecurity): SecurityFilterChain = {

    http
      .csrf(_.disable())
      .sessionManagement(session=>session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth => auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/asset/**").hasAnyRole("ADMIN","EMPLOYEE","TECH")
        .requestMatchers("/api/user/**"). hasAnyRole("ADMIN", "TECH", "EMPLOYEE")
        .requestMatchers("/api/complaint/**").hasAnyRole("ADMIN","TECH","EMPLOYEE")
        .requestMatchers("/api/asset-request/**").hasAnyRole("ADMIN","EMPLOYEE")
        .requestMatchers("/api/asset-assignment/**").hasAnyRole("EMPLOYEE","ADMIN")
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthenticationFilter,classOf[UsernamePasswordAuthenticationFilter])
    http.build()
  }

  @Bean
  def userDetailsService(): UserDetailsService = {
    new InMemoryUserDetailsManager()
  }

}
