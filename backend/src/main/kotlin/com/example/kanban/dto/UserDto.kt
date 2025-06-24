package com.example.kanban.dto

import java.time.LocalDateTime

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val user: UserResponseDto,
    val token: String
)

data class UserResponseDto(
    val id: Long,
    val name: String,
    val email: String,
    val lastLogin: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserCreateDto(
    val name: String,
    val email: String,
    val password: String
)

data class UserUpdateDto(
    val name: String? = null,
    val email: String? = null
)