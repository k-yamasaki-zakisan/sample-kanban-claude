package com.example.kanban.controller

import com.example.kanban.dto.*
import com.example.kanban.service.UserService
import com.example.kanban.service.JwtService
import com.example.kanban.model.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val user = userService.authenticate(loginRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponseDto(
                    user = UserResponseDto(0, "", "", null, 
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()),
                    token = ""
                ))

        val token = jwtService.generateToken(user.email, user.id)
        val loginResponse = LoginResponseDto(user = user, token = token)

        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponseDto> {
        val user = authentication.principal as? User
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val userResponse = UserResponseDto(
            id = user.id,
            name = user.name,
            email = user.email,
            lastLogin = user.lastLogin,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )

        return ResponseEntity.ok(userResponse)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<Any> {
        return try {
            val user = userService.createUser(userCreateDto)
            ResponseEntity.status(HttpStatus.CREATED).body(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        }
    }
}