package com.example.kanban.dto

import com.example.kanban.model.TaskStatus
import java.time.LocalDateTime

data class TaskCreateDto(
    val title: String,
    val description: String? = null
)

data class TaskUpdateDto(
    val title: String? = null,
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