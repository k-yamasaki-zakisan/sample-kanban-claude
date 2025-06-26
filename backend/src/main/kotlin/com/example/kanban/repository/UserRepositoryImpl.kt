package com.example.kanban.repository

import com.example.kanban.model.User
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional

@Repository
@Transactional
class UserRepositoryImpl : UserRepository {
    
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    
    override fun findById(id: Long): User? {
        return entityManager.find(User::class.java, id)
    }
    
    override fun save(user: User): User {
        return if (user.id == 0L) {
            val newUser = user.copy(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            entityManager.persist(newUser)
            newUser
        } else {
            val updatedUser = user.copy(updatedAt = LocalDateTime.now())
            entityManager.merge(updatedUser)
        }
    }
    
    override fun deleteById(id: Long) {
        val jpql = "DELETE FROM User u WHERE u.id = :id"
        entityManager.createQuery(jpql)
            .setParameter("id", id)
            .executeUpdate()
    }
    
    override fun findByEmail(email: String): User? {
        val jpql = "SELECT u FROM User u WHERE u.email = :email"
        return try {
            entityManager.createQuery(jpql, User::class.java)
                .setParameter("email", email)
                .singleResult
        } catch (e: Exception) {
            null
        }
    }
    
    override fun existsByEmail(email: String): Boolean {
        val jpql = "SELECT COUNT(u) FROM User u WHERE u.email = :email"
        val count = entityManager.createQuery(jpql, Long::class.java)
            .setParameter("email", email)
            .singleResult
        return count > 0
    }
    
    override fun updateLastLogin(id: Long, lastLogin: LocalDateTime, updatedAt: LocalDateTime): Boolean {
        val jpql = "UPDATE User u SET u.lastLogin = :lastLogin, u.updatedAt = :updatedAt WHERE u.id = :id"
        val updatedRows = entityManager.createQuery(jpql)
            .setParameter("id", id)
            .setParameter("lastLogin", lastLogin)
            .setParameter("updatedAt", updatedAt)
            .executeUpdate()
        return updatedRows > 0
    }
    
    override fun updateProfile(id: Long, name: String?, email: String?): User? {
        val user = findById(id) ?: return null
        
        val updatedUser = user.copy(
            name = name ?: user.name,
            email = email ?: user.email,
            updatedAt = LocalDateTime.now()
        )
        
        return entityManager.merge(updatedUser)
    }
    
    override fun findActiveUsersOrderByLastLogin(): List<User> {
        val jpql = """
            SELECT u FROM User u 
            WHERE u.lastLogin IS NOT NULL 
            ORDER BY u.lastLogin DESC
        """.trimIndent()
        return entityManager.createQuery(jpql, User::class.java)
            .setMaxResults(1000) // 安全のため最大1000件に制限
            .resultList
    }
    
    override fun countActiveUsers(): Long {
        val jpql = "SELECT COUNT(u) FROM User u WHERE u.lastLogin IS NOT NULL"
        return entityManager.createQuery(jpql, Long::class.java).singleResult
    }
    
    override fun findUsersRegisteredInPeriod(startDate: LocalDateTime, endDate: LocalDateTime): List<User> {
        val jpql = """
            SELECT u FROM User u 
            WHERE u.createdAt >= :startDate 
            AND u.createdAt <= :endDate 
            ORDER BY u.createdAt DESC
        """.trimIndent()
        return entityManager.createQuery(jpql, User::class.java)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .resultList
    }
}