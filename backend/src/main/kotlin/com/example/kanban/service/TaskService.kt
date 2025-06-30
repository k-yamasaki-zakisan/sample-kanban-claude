package com.example.kanban.service

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import com.example.kanban.repository.TaskRepository
import com.example.kanban.repository.TaskImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskImageService: TaskImageService
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
        val savedTask = taskRepository.save(task)
        
        // descriptionに含まれる画像IDを抽出して永続化
        if (!taskCreateDto.description.isNullOrBlank()) {
            val imageIds = taskImageService.extractImageIdsFromMarkdown(taskCreateDto.description)
            if (imageIds.isNotEmpty()) {
                taskImageService.markImagesAsUsed(imageIds, userId, taskCreateDto.description)
            }
        }
        
        return savedTask.toResponseDto()
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
        
        val savedTask = taskRepository.save(updatedTask)
        
        // descriptionが更新された場合、新しい画像IDを処理
        if (taskUpdateDto.description != null) {
            val imageIds = taskImageService.extractImageIdsFromMarkdown(taskUpdateDto.description)
            if (imageIds.isNotEmpty()) {
                taskImageService.markImagesAsUsed(imageIds, userId, taskUpdateDto.description)
            }
        }
        
        return savedTask.toResponseDto()
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
        // descriptionから画像IDを抽出
        val imageIds = if (!this.description.isNullOrBlank()) {
            taskImageService.extractImageIdsFromMarkdown(this.description)
        } else {
            emptyList()
        }
        
        // 抽出した画像IDに基づいて画像情報を取得
        val images = if (imageIds.isNotEmpty()) {
            taskImageService.getUserImages(this.userId, includeTemporary = false)
                .filter { it.id in imageIds }
                .sortedBy { imageIds.indexOf(it.id) }
        } else {
            emptyList()
        }
        
        return TaskResponseDto(
            id = this.id,
            title = this.title,
            description = this.description,
            status = this.status,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            images = images
        )
    }
}