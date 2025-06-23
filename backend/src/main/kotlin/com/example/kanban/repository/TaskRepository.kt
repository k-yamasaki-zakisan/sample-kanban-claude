package com.example.kanban.repository

import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findByStatus(status: TaskStatus): List<Task>
    fun findByOrderByCreatedAtDesc(): List<Task>
}