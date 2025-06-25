package com.example.kanban.service

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import com.example.kanban.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository
) {
    
    // ユーザー別のタスク一覧取得
    fun getAllTasksByUser(userId: Long): List<TaskResponseDto> {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .map { it.toResponseDto() }
    }
    
    // ユーザー別のタスク詳細取得
    fun getTaskByUserAndId(userId: Long, id: Long): TaskResponseDto {
        return taskRepository.findByUserIdAndId(userId, id)
            ?.toResponseDto()
            ?: throw IllegalArgumentException("Task not found or access denied for user $userId")
    }
    
    // ユーザー別のタスク作成
    @Transactional
    fun createTaskForUser(taskCreateDto: TaskCreateDto, userId: Long): TaskResponseDto {
        val task = Task(
            title = taskCreateDto.title,
            description = taskCreateDto.description,
            userId = userId
        )
        return taskRepository.save(task).toResponseDto()
    }
    
    // ユーザー別のタスク更新
    @Transactional
    fun updateTaskForUser(id: Long, taskUpdateDto: TaskUpdateDto, userId: Long): TaskResponseDto {
        val existingTask = taskRepository.findByUserIdAndId(userId, id)
            ?: throw IllegalArgumentException("Task not found or access denied for user $userId and task $id")
        
        val updatedTask = existingTask.copy(
            title = taskUpdateDto.title ?: existingTask.title,
            description = taskUpdateDto.description ?: existingTask.description,
            status = taskUpdateDto.status ?: existingTask.status,
            updatedAt = LocalDateTime.now()
        )
        
        return taskRepository.save(updatedTask).toResponseDto()
    }
    
    // ユーザー別のタスク削除
    @Transactional
    fun deleteTaskForUser(id: Long, userId: Long): Boolean {
        if (!taskRepository.existsByUserIdAndId(userId, id)) {
            throw IllegalArgumentException("Task not found or access denied for user $userId and task $id")
        }
        taskRepository.deleteById(id)
        return true
    }
    
    // ユーザー別のステータス別タスク取得
    fun getTasksByUserAndStatus(userId: Long, status: TaskStatus): List<TaskResponseDto> {
        return taskRepository.findByUserIdAndStatus(userId, status)
            .map { it.toResponseDto() }
    }
    
    private fun Task.toResponseDto(): TaskResponseDto {
        return TaskResponseDto(
            id = this.id,
            title = this.title,
            description = this.description,
            status = this.status,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}