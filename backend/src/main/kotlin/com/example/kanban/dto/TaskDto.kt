package com.example.kanban.dto

import com.example.kanban.model.TaskStatus
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class TaskCreateDto(
    @field:NotBlank(message = "タスクタイトルは必須です")
    @field:Size(min = 1, max = 200, message = "タイトルは1文字以上200文字以内で入力してください")
    val title: String,
    
    @field:Size(max = 1000, message = "説明は1000文字以内で入力してください")
    val description: String? = null
)

data class TaskUpdateDto(
    @field:Size(min = 1, max = 200, message = "タイトルは1文字以上200文字以内で入力してください")
    val title: String? = null,
    
    @field:Size(max = 1000, message = "説明は1000文字以内で入力してください")
    val description: String? = null,
    
    val status: TaskStatus? = null
)

data class TaskResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)