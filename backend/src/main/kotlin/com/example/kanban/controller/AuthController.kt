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
    fun login(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<out Any> {
        val user = userService.authenticate(loginRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto("Invalid email or password"))

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
        val user = authentication.principal as? UserResponseDto
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(user)
    }

    @PutMapping("/me")
    fun updateCurrentUser(
        @Valid @RequestBody userUpdateDto: UserUpdateDto,
        authentication: Authentication
    ): ResponseEntity<out Any> {
        val currentUser = authentication.principal as? UserResponseDto
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val updatedUser = userService.updateUser(currentUser.id, userUpdateDto)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseDto("User not found"))

            // ユーザー情報が更新されたので新しいJWTトークンを生成
            val newToken = jwtService.generateToken(updatedUser.email, updatedUser.id)
            val updateResponse = UserUpdateResponseDto(user = updatedUser, token = newToken)

            ResponseEntity.ok(updateResponse)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto(e.message ?: "Update failed"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto("Internal server error"))
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody userCreateDto: UserCreateDto): ResponseEntity<out Any> {
        return try {
            val user = userService.createUser(userCreateDto)
            ResponseEntity.status(HttpStatus.CREATED).body(user)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        }
    }
}