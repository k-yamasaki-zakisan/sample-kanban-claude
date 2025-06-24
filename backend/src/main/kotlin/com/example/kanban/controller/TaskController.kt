package com.example.kanban.controller

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.dto.UserResponseDto
import com.example.kanban.model.TaskStatus
import com.example.kanban.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {
    
    @GetMapping
    fun getAllTasks(authentication: Authentication): List<TaskResponseDto> {
        val user = authentication.principal as UserResponseDto
        return taskService.getAllTasksByUser(user.id)
    }
    
    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long, authentication: Authentication): ResponseEntity<TaskResponseDto> {
        val user = authentication.principal as UserResponseDto
        return try {
            val task = taskService.getTaskByUserAndId(user.id, id)
            ResponseEntity.ok(task)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createTask(
        @Valid @RequestBody taskCreateDto: TaskCreateDto,
        authentication: Authentication
    ): ResponseEntity<TaskResponseDto> {
        val user = authentication.principal as UserResponseDto
        val createdTask = taskService.createTaskForUser(taskCreateDto, user.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }
    
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long, 
        @Valid @RequestBody taskUpdateDto: TaskUpdateDto,
        authentication: Authentication
    ): ResponseEntity<TaskResponseDto> {
        val user = authentication.principal as UserResponseDto
        return try {
            val updatedTask = taskService.updateTaskForUser(id, taskUpdateDto, user.id)
            ResponseEntity.ok(updatedTask)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long, authentication: Authentication): ResponseEntity<Void> {
        val user = authentication.principal as UserResponseDto
        return try {
            taskService.deleteTaskForUser(id, user.id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/status/{status}")
    fun getTasksByStatus(@PathVariable status: TaskStatus, authentication: Authentication): List<TaskResponseDto> {
        val user = authentication.principal as UserResponseDto
        return taskService.getTasksByUserAndStatus(user.id, status)
    }
}