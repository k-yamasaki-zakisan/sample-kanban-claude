package com.example.kanban.service

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import com.example.kanban.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository
) {
    
    fun getAllTasks(): List<TaskResponseDto> {
        return taskRepository.findByOrderByCreatedAtDesc()
            .map { it.toResponseDto() }
    }
    
    fun getTaskById(id: Long): TaskResponseDto? {
        return taskRepository.findById(id)
            .map { it.toResponseDto() }
            .orElse(null)
    }
    
    fun createTask(taskCreateDto: TaskCreateDto): TaskResponseDto {
        val task = Task(
            title = taskCreateDto.title,
            description = taskCreateDto.description
        )
        return taskRepository.save(task).toResponseDto()
    }
    
    fun updateTask(id: Long, taskUpdateDto: TaskUpdateDto): TaskResponseDto? {
        return taskRepository.findById(id)
            .map { existingTask ->
                val updatedTask = existingTask.copy(
                    title = taskUpdateDto.title ?: existingTask.title,
                    description = taskUpdateDto.description ?: existingTask.description,
                    status = taskUpdateDto.status ?: existingTask.status,
                    updatedAt = LocalDateTime.now()
                )
                taskRepository.save(updatedTask).toResponseDto()
            }
            .orElse(null)
    }
    
    fun deleteTask(id: Long): Boolean {
        return if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id)
            true
        } else {
            false
        }
    }
    
    fun getTasksByStatus(status: TaskStatus): List<TaskResponseDto> {
        return taskRepository.findByStatus(status)
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