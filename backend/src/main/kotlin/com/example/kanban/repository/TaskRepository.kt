package com.example.kanban.repository

import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findByStatus(status: TaskStatus): List<Task>
    fun findByOrderByCreatedAtDesc(): List<Task>
    
    // ユーザーID別のタスク取得メソッド
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Task>
    fun findByUserIdAndStatus(userId: Long, status: TaskStatus): List<Task>
    fun findByUserIdAndId(userId: Long, id: Long): Task?
    
    // ユーザーID別のタスク存在確認
    fun existsByUserIdAndId(userId: Long, id: Long): Boolean
}