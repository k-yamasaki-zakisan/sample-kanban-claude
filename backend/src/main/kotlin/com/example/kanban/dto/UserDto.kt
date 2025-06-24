package com.example.kanban.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class LoginRequestDto(
    @field:NotBlank(message = "メールアドレスは必須です")
    @field:Email(message = "正しいメールアドレス形式で入力してください")
    @field:Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    val email: String,
    
    @field:NotBlank(message = "パスワードは必須です")
    @field:Size(min = 6, max = 100, message = "パスワードは6文字以上100文字以内で入力してください")
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
    @field:NotBlank(message = "ユーザー名は必須です")
    @field:Size(min = 1, max = 100, message = "ユーザー名は1文字以上100文字以内で入力してください")
    val name: String,
    
    @field:NotBlank(message = "メールアドレスは必須です")
    @field:Email(message = "正しいメールアドレス形式で入力してください")
    @field:Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    val email: String,
    
    @field:NotBlank(message = "パスワードは必須です")
    @field:Size(min = 6, max = 100, message = "パスワードは6文字以上100文字以内で入力してください")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$",
        message = "パスワードは英数字を含む6文字以上で入力してください"
    )
    val password: String
)

data class UserUpdateDto(
    @field:Size(min = 1, max = 100, message = "ユーザー名は1文字以上100文字以内で入力してください")
    val name: String? = null,
    
    @field:Email(message = "正しいメールアドレス形式で入力してください")
    @field:Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    val email: String? = null
)