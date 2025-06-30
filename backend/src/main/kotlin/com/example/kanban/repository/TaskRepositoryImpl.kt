package com.example.kanban.repository

import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional

@Repository
@Transactional
class TaskRepositoryImpl : TaskRepository {
    
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    
    override fun findById(id: Long): Task? {
        return entityManager.find(Task::class.java, id)
    }
    
    override fun save(task: Task): Task {
        return if (task.id == 0L) {
            val newTask = task.copy(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            entityManager.persist(newTask)
            newTask
        } else {
            val updatedTask = task.copy(updatedAt = LocalDateTime.now())
            entityManager.merge(updatedTask)
        }
    }
    
    override fun deleteById(id: Long) {
        val jpql = "DELETE FROM Task t WHERE t.id = :id"
        entityManager.createQuery(jpql)
            .setParameter("id", id)
            .executeUpdate()
    }
    
    override fun findByStatus(status: TaskStatus): List<Task> {
        val jpql = "SELECT t FROM Task t WHERE t.status = :status ORDER BY t.createdAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("status", status)
            .setMaxResults(1000) // 安全のため最大1000件に制限
            .resultList
    }
    
    override fun findByOrderByCreatedAtDesc(): List<Task> {
        val jpql = "SELECT t FROM Task t ORDER BY t.createdAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setMaxResults(1000) // 安全のため最大1000件に制限
            .resultList
    }
    
    override fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Task> {
        val jpql = "SELECT t FROM Task t WHERE t.userId = :userId ORDER BY t.createdAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("userId", userId)
            .resultList
    }
    
    override fun findByUserIdAndStatus(userId: Long, status: TaskStatus): List<Task> {
        val jpql = "SELECT t FROM Task t WHERE t.userId = :userId AND t.status = :status ORDER BY t.createdAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("userId", userId)
            .setParameter("status", status)
            .resultList
    }
    
    override fun findByUserIdAndId(userId: Long, id: Long): Task? {
        val jpql = "SELECT t FROM Task t WHERE t.userId = :userId AND t.id = :id"
        return try {
            entityManager.createQuery(jpql, Task::class.java)
                .setParameter("userId", userId)
                .setParameter("id", id)
                .singleResult
        } catch (e: Exception) {
            null
        }
    }
    
    override fun existsByUserIdAndId(userId: Long, id: Long): Boolean {
        val jpql = "SELECT COUNT(t) FROM Task t WHERE t.userId = :userId AND t.id = :id"
        val count = entityManager.createQuery(jpql, Long::class.java)
            .setParameter("userId", userId)
            .setParameter("id", id)
            .singleResult
        return count > 0
    }
    
    override fun countByUserId(userId: Long): Long {
        val jpql = "SELECT COUNT(t) FROM Task t WHERE t.userId = :userId"
        return entityManager.createQuery(jpql, Long::class.java)
            .setParameter("userId", userId)
            .singleResult
    }
    
    override fun countByUserIdAndStatus(userId: Long, status: TaskStatus): Long {
        val jpql = "SELECT COUNT(t) FROM Task t WHERE t.userId = :userId AND t.status = :status"
        return entityManager.createQuery(jpql, Long::class.java)
            .setParameter("userId", userId)
            .setParameter("status", status)
            .singleResult
    }
    
    override fun findByUserIdAndStatusOrderByUpdatedAtDesc(userId: Long, status: TaskStatus): List<Task> {
        val jpql = "SELECT t FROM Task t WHERE t.userId = :userId AND t.status = :status ORDER BY t.updatedAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("userId", userId)
            .setParameter("status", status)
            .resultList
    }
    
    override fun findRecentTasksByUserId(userId: Long, limit: Int): List<Task> {
        val jpql = "SELECT t FROM Task t WHERE t.userId = :userId ORDER BY t.updatedAt DESC"
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("userId", userId)
            .setMaxResults(limit)
            .resultList
    }
    
    override fun findTasksCreatedInPeriod(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Task> {
        val jpql = """
            SELECT t FROM Task t 
            WHERE t.userId = :userId 
            AND t.createdAt >= :startDate 
            AND t.createdAt <= :endDate 
            ORDER BY t.createdAt DESC
        """.trimIndent()
        return entityManager.createQuery(jpql, Task::class.java)
            .setParameter("userId", userId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .resultList
    }

    override fun findByIdAndUserId(id: Long, userId: Long): Task? {
        return findByUserIdAndId(userId, id)
    }
}