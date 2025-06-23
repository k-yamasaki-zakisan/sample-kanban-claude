package com.example.kanban.controller

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.TaskStatus
import com.example.kanban.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = ["http://localhost:3000"])
class TaskController(
    private val taskService: TaskService
) {
    
    @GetMapping
    fun getAllTasks(): List<TaskResponseDto> {
        return taskService.getAllTasks()
    }
    
    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): ResponseEntity<TaskResponseDto> {
        val task = taskService.getTaskById(id)
        return if (task != null) {
            ResponseEntity.ok(task)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createTask(@RequestBody taskCreateDto: TaskCreateDto): ResponseEntity<TaskResponseDto> {
        val createdTask = taskService.createTask(taskCreateDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }
    
    @PutMapping("/{id}")
    fun updateTask(@PathVariable id: Long, @RequestBody taskUpdateDto: TaskUpdateDto): ResponseEntity<TaskResponseDto> {
        val updatedTask = taskService.updateTask(id, taskUpdateDto)
        return if (updatedTask != null) {
            ResponseEntity.ok(updatedTask)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        return if (taskService.deleteTask(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/status/{status}")
    fun getTasksByStatus(@PathVariable status: TaskStatus): List<TaskResponseDto> {
        return taskService.getTasksByStatus(status)
    }
}