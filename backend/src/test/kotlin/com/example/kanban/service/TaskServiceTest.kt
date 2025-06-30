package com.example.kanban.service

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskResponseDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.Task
import com.example.kanban.model.TaskStatus
import com.example.kanban.repository.TaskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.mockito.Mockito.lenient
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var taskImageService: TaskImageService

    @InjectMocks
    private lateinit var taskService: TaskService

    private lateinit var testTask: Task
    private lateinit var testTasks: List<Task>
    private val testUserId = 1L

    @BeforeEach
    fun setUp() {
        // Set lenient mode for all mocks
        lenient().whenever(taskImageService.extractImageIdsFromMarkdown(anyOrNull())).thenReturn(emptyList())
        lenient().whenever(taskImageService.getUserImages(any(), any())).thenReturn(emptyList())
        lenient().doNothing().whenever(taskImageService).markImagesAsUsed(any(), any(), any())

        testTask = Task(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            status = TaskStatus.TODO,
            userId = testUserId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        testTasks = listOf(
            testTask,
            Task(
                id = 2L,
                title = "Test Task 2",
                description = "Test Description 2",
                status = TaskStatus.IN_PROGRESS,
                userId = testUserId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Task(
                id = 3L,
                title = "Test Task 3",
                description = "Test Description 3",
                status = TaskStatus.DONE,
                userId = testUserId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    @Test
    fun `getAllTasksByUser should return all tasks for user ordered by creation date desc`() {
        // Given
        whenever(taskRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(testTasks)

        // When
        val result = taskService.getAllTasksByUser(testUserId)

        // Then
        assertEquals(3, result.size)
        assertEquals("Test Task", result[0].title)
        assertEquals("Test Task 2", result[1].title)
        assertEquals("Test Task 3", result[2].title)
        verify(taskRepository).findByUserIdOrderByCreatedAtDesc(testUserId)
    }

    @Test
    fun `getAllTasksByUser should return empty list when no tasks exist for user`() {
        // Given
        whenever(taskRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(emptyList())

        // When
        val result = taskService.getAllTasksByUser(testUserId)

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).findByUserIdOrderByCreatedAtDesc(testUserId)
    }

    @Test
    fun `getTaskByUserAndId should return task when exists for user`() {
        // Given
        whenever(taskRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(testTask)

        // When
        val result = taskService.getTaskByUserAndId(testUserId, 1L)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Task", result.title)
        assertEquals("Test Description", result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).findByUserIdAndId(testUserId, 1L)
    }

    @Test
    fun `getTaskByUserAndId should throw exception when task not found or access denied`() {
        // Given
        whenever(taskRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(null)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            taskService.getTaskByUserAndId(testUserId, 1L)
        }
        assertEquals("Task not found or access denied for user $testUserId", exception.message)
        verify(taskRepository).findByUserIdAndId(testUserId, 1L)
    }

    @Test
    fun `createTaskForUser should save and return created task`() {
        // Given
        val taskCreateDto = TaskCreateDto(
            title = "New Task",
            description = "New Description"
        )
        val savedTask = Task(
            id = 4L,
            title = "New Task",
            description = "New Description",
            status = TaskStatus.TODO,
            userId = testUserId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        whenever(taskRepository.save(any<Task>())).thenReturn(savedTask)

        // When
        val result = taskService.createTaskForUser(taskCreateDto, testUserId)

        // Then
        assertEquals(4L, result.id)
        assertEquals("New Task", result.title)
        assertEquals("New Description", result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `createTaskForUser should set default values correctly`() {
        // Given
        val taskCreateDto = TaskCreateDto(
            title = "New Task",
            description = null
        )
        val savedTask = Task(
            id = 4L,
            title = "New Task",
            description = null,
            status = TaskStatus.TODO,
            userId = testUserId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        whenever(taskRepository.save(any<Task>())).thenReturn(savedTask)

        // When
        val result = taskService.createTaskForUser(taskCreateDto, testUserId)

        // Then
        assertEquals("New Task", result.title)
        assertNull(result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `updateTaskForUser should update and return task when exists`() {
        // Given
        val taskUpdateDto = TaskUpdateDto(
            title = "Updated Task",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS
        )
        val updatedTask = testTask.copy(
            title = "Updated Task",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS,
            updatedAt = LocalDateTime.now()
        )
        whenever(taskRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(testTask)
        whenever(taskRepository.save(any<Task>())).thenReturn(updatedTask)

        // When
        val result = taskService.updateTaskForUser(1L, taskUpdateDto, testUserId)

        // Then
        assertEquals("Updated Task", result.title)
        assertEquals("Updated Description", result.description)
        assertEquals(TaskStatus.IN_PROGRESS, result.status)
        verify(taskRepository).findByUserIdAndId(testUserId, 1L)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `updateTaskForUser should update only provided fields`() {
        // Given
        val taskUpdateDto = TaskUpdateDto(
            title = "Updated Task",
            description = null,
            status = null
        )
        val updatedTask = testTask.copy(
            title = "Updated Task",
            updatedAt = LocalDateTime.now()
        )
        whenever(taskRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(testTask)
        whenever(taskRepository.save(any<Task>())).thenReturn(updatedTask)

        // When
        val result = taskService.updateTaskForUser(1L, taskUpdateDto, testUserId)

        // Then
        assertEquals("Updated Task", result.title)
        assertEquals("Test Description", result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).findByUserIdAndId(testUserId, 1L)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `updateTaskForUser should throw exception when task not found`() {
        // Given
        val taskUpdateDto = TaskUpdateDto(
            title = "Updated Task",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS
        )
        whenever(taskRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(null)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            taskService.updateTaskForUser(1L, taskUpdateDto, testUserId)
        }
        assertEquals("Task not found or access denied for user $testUserId and task 1", exception.message)
        verify(taskRepository).findByUserIdAndId(testUserId, 1L)
        verify(taskRepository, never()).save(any<Task>())
    }

    @Test
    fun `deleteTaskForUser should delete task when exists`() {
        // Given
        whenever(taskRepository.existsByUserIdAndId(testUserId, 1L)).thenReturn(true)

        // When
        val result = taskService.deleteTaskForUser(1L, testUserId)

        // Then
        assertTrue(result)
        verify(taskRepository).existsByUserIdAndId(testUserId, 1L)
        verify(taskRepository).deleteById(1L)
    }

    @Test
    fun `deleteTaskForUser should throw exception when task not found`() {
        // Given
        whenever(taskRepository.existsByUserIdAndId(testUserId, 1L)).thenReturn(false)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            taskService.deleteTaskForUser(1L, testUserId)
        }
        assertEquals("Task not found or access denied for user $testUserId and task 1", exception.message)
        verify(taskRepository).existsByUserIdAndId(testUserId, 1L)
        verify(taskRepository, never()).deleteById(1L)
    }

    @Test
    fun `getTasksByUserAndStatus should return tasks filtered by status`() {
        // Given
        val todoTasks = listOf(testTask)
        whenever(taskRepository.findByUserIdAndStatus(testUserId, TaskStatus.TODO)).thenReturn(todoTasks)

        // When
        val result = taskService.getTasksByUserAndStatus(testUserId, TaskStatus.TODO)

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskStatus.TODO, result[0].status)
        assertEquals("Test Task", result[0].title)
        verify(taskRepository).findByUserIdAndStatus(testUserId, TaskStatus.TODO)
    }

    @Test
    fun `getTasksByUserAndStatus should return empty list when no tasks match status`() {
        // Given
        whenever(taskRepository.findByUserIdAndStatus(testUserId, TaskStatus.DONE)).thenReturn(emptyList())

        // When
        val result = taskService.getTasksByUserAndStatus(testUserId, TaskStatus.DONE)

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).findByUserIdAndStatus(testUserId, TaskStatus.DONE)
    }

    @Test
    fun `getTasksByUserAndStatus should handle different statuses correctly`() {
        // Given
        val inProgressTasks = listOf(
            Task(
                id = 2L,
                title = "In Progress Task",
                description = "Description",
                status = TaskStatus.IN_PROGRESS,
                userId = testUserId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        whenever(taskRepository.findByUserIdAndStatus(testUserId, TaskStatus.IN_PROGRESS)).thenReturn(inProgressTasks)

        // When
        val result = taskService.getTasksByUserAndStatus(testUserId, TaskStatus.IN_PROGRESS)

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskStatus.IN_PROGRESS, result[0].status)
        assertEquals("In Progress Task", result[0].title)
        verify(taskRepository).findByUserIdAndStatus(testUserId, TaskStatus.IN_PROGRESS)
    }
}