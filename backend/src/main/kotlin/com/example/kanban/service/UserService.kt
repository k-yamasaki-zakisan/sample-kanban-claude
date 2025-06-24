package com.example.kanban.service

import com.example.kanban.dto.*
import com.example.kanban.model.User
import com.example.kanban.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    private val passwordEncoder: PasswordEncoder by lazy {
        val environment = System.getenv("ENVIRONMENT") ?: "local"
        if (environment != "local") {
            BCryptPasswordEncoder(12) // クラウド環境では強度を上げる
        } else {
            BCryptPasswordEncoder(10) // ローカル環境では開発速度を優先
        }
    }

    @Transactional
    fun authenticate(loginRequest: LoginRequestDto): UserResponseDto? {
        val user = userRepository.findByEmail(loginRequest.email)
            .orElse(null) ?: return null

        return if (passwordEncoder.matches(loginRequest.password, user.password)) {
            updateLastLogin(user.id)
            convertToDto(user.copy(lastLogin = LocalDateTime.now()))
        } else {
            null
        }
    }

    fun findByEmail(email: String): UserResponseDto? {
        return userRepository.findByEmail(email)
            .map { convertToDto(it) }
            .orElse(null)
    }

    fun findById(id: Long): UserResponseDto? {
        return userRepository.findById(id)
            .map { convertToDto(it) }
            .orElse(null)
    }

    @Transactional
    fun createUser(userCreateDto: UserCreateDto): UserResponseDto {
        if (userRepository.existsByEmail(userCreateDto.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val encodedPassword = passwordEncoder.encode(userCreateDto.password)
        val user = User(
            name = userCreateDto.name,
            email = userCreateDto.email,
            password = encodedPassword
        )

        val savedUser = userRepository.save(user)
        return convertToDto(savedUser)
    }

    @Transactional
    fun updateLastLogin(userId: Long) {
        val now = LocalDateTime.now()
        userRepository.updateLastLogin(userId, now, now)
    }

    private fun convertToDto(user: User): UserResponseDto {
        return UserResponseDto(
            id = user.id,
            name = user.name,
            email = user.email,
            lastLogin = user.lastLogin,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}