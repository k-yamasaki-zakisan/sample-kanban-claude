package com.example.kanban.config

import com.example.kanban.service.JwtService
import com.example.kanban.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        println("JWT Filter - Request: ${request.method} ${request.requestURI}")
        println("JWT Filter - Auth Header: $authHeader")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            println("JWT Filter - No valid auth header found")
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        val userId = jwtService.extractUserId(token)
        println("JWT Filter - Extracted userId: $userId")

        if (userId != null && SecurityContextHolder.getContext().authentication == null) {
            if (jwtService.validateToken(token)) {
                val user = userService.findById(userId)
                println("JWT Filter - User found: ${user?.email}")
                if (user != null) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        emptyList()
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                    println("JWT Filter - Authentication set successfully")
                } else {
                    println("JWT Filter - User not found for userId: $userId")
                }
            } else {
                println("JWT Filter - Token validation failed")
            }
        } else {
            println("JWT Filter - UserId is null or authentication already exists")
        }

        filterChain.doFilter(request, response)
    }
}