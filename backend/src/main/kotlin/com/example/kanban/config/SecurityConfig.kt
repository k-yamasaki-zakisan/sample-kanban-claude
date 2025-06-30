package com.example.kanban.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter): SecurityFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { csrf ->
                if (isCloudEnvironment()) {
                    csrf.ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
                } else {
                    csrf.disable()
                }
            }
            .headers { headers ->
                headers
                    .frameOptions { it.deny() }
                    .contentTypeOptions { }
                    .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                if (isCloudEnvironment()) {
                    headers.httpStrictTransportSecurity { }
                }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout").permitAll()
                    .requestMatchers("/api/auth/me").authenticated()
                    .requestMatchers("/actuator/health", "/api/healthcheck").permitAll()
                    .requestMatchers("/api/tasks/**").authenticated()
                    .requestMatchers("/api/images/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .let { http ->
                if (isCloudEnvironment()) {
                    http.requiresChannel { channel ->
                        channel.anyRequest().requiresSecure()
                    }
                } else {
                    http
                }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val frontUrl = System.getenv("FRONT_URL") ?: "http://localhost:3000"
        
        configuration.allowedOrigins = listOf(frontUrl)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return if (isCloudEnvironment()) {
            BCryptPasswordEncoder(12) // クラウド環境では強度を上げる
        } else {
            BCryptPasswordEncoder(10) // ローカル環境では開発速度を優先
        }
    }

    private fun isCloudEnvironment(): Boolean {
        val environment = System.getenv("ENVIRONMENT") ?: "local"
        return environment != "local"
    }
}