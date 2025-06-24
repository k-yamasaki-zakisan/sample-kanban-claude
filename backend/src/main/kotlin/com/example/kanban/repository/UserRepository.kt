package com.example.kanban.repository

import com.example.kanban.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByEmail(email: String): Optional<User>
    
    fun existsByEmail(email: String): Boolean
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.updatedAt = :updatedAt WHERE u.id = :id")
    fun updateLastLogin(
        @Param("id") id: Long,
        @Param("lastLogin") lastLogin: LocalDateTime,
        @Param("updatedAt") updatedAt: LocalDateTime
    )
}