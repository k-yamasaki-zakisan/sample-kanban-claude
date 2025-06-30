package com.example.kanban.repository

import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository {
    fun findById(id: Long): Task?
    fun save(task: Task): Task
    fun deleteById(id: Long)
    fun findByStatus(status: TaskStatus): List<Task>
    fun findByOrderByCreatedAtDesc(): List<Task>
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Task>
    fun findByUserIdAndStatus(userId: Long, status: TaskStatus): List<Task>
    fun findByUserIdAndId(userId: Long, id: Long): Task?
    fun findByIdAndUserId(id: Long, userId: Long): Task?
    fun existsByUserIdAndId(userId: Long, id: Long): Boolean
    fun countByUserId(userId: Long): Long
    fun countByUserIdAndStatus(userId: Long, status: TaskStatus): Long
    fun findByUserIdAndStatusOrderByUpdatedAtDesc(userId: Long, status: TaskStatus): List<Task>
    fun findRecentTasksByUserId(userId: Long, limit: Int): List<Task>
    fun findTasksCreatedInPeriod(userId: Long, startDate: java.time.LocalDateTime, endDate: java.time.LocalDateTime): List<Task>
}