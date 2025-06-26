package com.example.kanban.repository

import com.example.kanban.model.User
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository {
    fun findById(id: Long): User?
    fun save(user: User): User
    fun deleteById(id: Long)
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun updateLastLogin(id: Long, lastLogin: LocalDateTime, updatedAt: LocalDateTime): Boolean
    fun updateProfile(id: Long, name: String?, email: String?): User?
    fun findActiveUsersOrderByLastLogin(): List<User>
    fun countActiveUsers(): Long
    fun findUsersRegisteredInPeriod(startDate: LocalDateTime, endDate: LocalDateTime): List<User>
}