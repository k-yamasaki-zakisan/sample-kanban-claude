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
        val user = userRepository.findByEmail(loginRequest.email) ?: return null

        return if (passwordEncoder.matches(loginRequest.password, user.password)) {
            updateLastLogin(user.id)
            convertToDto(user.copy(lastLogin = LocalDateTime.now()))
        } else {
            null
        }
    }

    fun findByEmail(email: String): UserResponseDto? {
        return userRepository.findByEmail(email)?.let { convertToDto(it) }
    }

    fun findById(id: Long): UserResponseDto? {
        return userRepository.findById(id)?.let { convertToDto(it) }
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

    @Transactional
    fun updateUser(userId: Long, userUpdateDto: UserUpdateDto): UserResponseDto? {
        // メールアドレスが変更される場合、重複チェック
        if (userUpdateDto.email != null) {
            val existingUser = userRepository.findByEmail(userUpdateDto.email)
            if (existingUser != null && existingUser.id != userId) {
                throw IllegalArgumentException("Email already exists")
            }
        }

        val updatedUser = userRepository.updateProfile(
            id = userId,
            name = userUpdateDto.name,
            email = userUpdateDto.email
        ) ?: return null

        return convertToDto(updatedUser)
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