package com.example.kanban.service

import com.example.kanban.dto.TaskCreateDto
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
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @InjectMocks
    private lateinit var taskService: TaskService

    private lateinit var testTask: Task
    private lateinit var testTasks: List<Task>

    @BeforeEach
    fun setUp() {
        testTask = Task(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            status = TaskStatus.TODO,
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
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Task(
                id = 3L,
                title = "Test Task 3",
                description = "Test Description 3",
                status = TaskStatus.IN_REVIEW,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    @Test
    fun `getAllTasks should return all tasks ordered by creation date desc`() {
        // Given
        `when`(taskRepository.findByOrderByCreatedAtDesc()).thenReturn(testTasks)

        // When
        val result = taskService.getAllTasks()

        // Then
        assertEquals(3, result.size)
        assertEquals("Test Task", result[0].title)
        assertEquals("Test Task 2", result[1].title)
        assertEquals("Test Task 3", result[2].title)
        verify(taskRepository).findByOrderByCreatedAtDesc()
    }

    @Test
    fun `getAllTasks should return empty list when no tasks exist`() {
        // Given
        `when`(taskRepository.findByOrderByCreatedAtDesc()).thenReturn(emptyList())

        // When
        val result = taskService.getAllTasks()

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).findByOrderByCreatedAtDesc()
    }

    @Test
    fun `getTaskById should return task when exists`() {
        // Given
        `when`(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))

        // When
        val result = taskService.getTaskById(1L)

        // Then
        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals("Test Task", result.title)
        assertEquals("Test Description", result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).findById(1L)
    }

    @Test
    fun `getTaskById should return null when task does not exist`() {
        // Given
        `when`(taskRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = taskService.getTaskById(999L)

        // Then
        assertNull(result)
        verify(taskRepository).findById(999L)
    }

    @Test
    fun `createTask should create and return new task`() {
        // Given
        val createDto = TaskCreateDto(
            title = "New Task",
            description = "New Description"
        )
        val savedTask = Task(
            id = 1L,
            title = "New Task",
            description = "New Description",
            status = TaskStatus.TODO,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(taskRepository.save(any(Task::class.java))).thenReturn(savedTask)

        // When
        val result = taskService.createTask(createDto)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("New Task", result.title)
        assertEquals("New Description", result.description)
        assertEquals(TaskStatus.TODO, result.status)
        verify(taskRepository).save(any(Task::class.java))
    }

    @Test
    fun `createTask should create task with null description`() {
        // Given
        val createDto = TaskCreateDto(
            title = "New Task",
            description = null
        )
        val savedTask = Task(
            id = 1L,
            title = "New Task",
            description = null,
            status = TaskStatus.TODO,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(taskRepository.save(any(Task::class.java))).thenReturn(savedTask)

        // When
        val result = taskService.createTask(createDto)

        // Then
        assertNotNull(result)
        assertEquals("New Task", result.title)
        assertNull(result.description)
        verify(taskRepository).save(any(Task::class.java))
    }

    @Test
    fun `updateTask should update existing task`() {
        // Given
        val updateDto = TaskUpdateDto(
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
        `when`(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))
        `when`(taskRepository.save(any(Task::class.java))).thenReturn(updatedTask)

        // When
        val result = taskService.updateTask(1L, updateDto)

        // Then
        assertNotNull(result)
        assertEquals("Updated Task", result!!.title)
        assertEquals("Updated Description", result.description)
        assertEquals(TaskStatus.IN_PROGRESS, result.status)
        verify(taskRepository).findById(1L)
        verify(taskRepository).save(any(Task::class.java))
    }

    @Test
    fun `updateTask should update only specified fields`() {
        // Given
        val updateDto = TaskUpdateDto(
            title = "Updated Title Only",
            description = null,
            status = null
        )
        val updatedTask = testTask.copy(
            title = "Updated Title Only",
            updatedAt = LocalDateTime.now()
        )
        `when`(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))
        `when`(taskRepository.save(any(Task::class.java))).thenReturn(updatedTask)

        // When
        val result = taskService.updateTask(1L, updateDto)

        // Then
        assertNotNull(result)
        assertEquals("Updated Title Only", result!!.title)
        assertEquals("Test Description", result.description) // Original description preserved
        assertEquals(TaskStatus.TODO, result.status) // Original status preserved
        verify(taskRepository).findById(1L)
        verify(taskRepository).save(any(Task::class.java))
    }

    @Test
    fun `updateTask should return null when task does not exist`() {
        // Given
        val updateDto = TaskUpdateDto(title = "Updated Task")
        `when`(taskRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = taskService.updateTask(999L, updateDto)

        // Then
        assertNull(result)
        verify(taskRepository).findById(999L)
        verify(taskRepository, never()).save(any(Task::class.java))
    }

    @Test
    fun `updateTask should update task status to IN_REVIEW`() {
        // Given
        val updateDto = TaskUpdateDto(status = TaskStatus.IN_REVIEW)
        val updatedTask = testTask.copy(
            status = TaskStatus.IN_REVIEW,
            updatedAt = LocalDateTime.now()
        )
        `when`(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))
        `when`(taskRepository.save(any(Task::class.java))).thenReturn(updatedTask)

        // When
        val result = taskService.updateTask(1L, updateDto)

        // Then
        assertNotNull(result)
        assertEquals(TaskStatus.IN_REVIEW, result!!.status)
        verify(taskRepository).findById(1L)
        verify(taskRepository).save(any(Task::class.java))
    }

    @Test
    fun `deleteTask should delete existing task and return true`() {
        // Given
        `when`(taskRepository.existsById(1L)).thenReturn(true)
        doNothing().`when`(taskRepository).deleteById(1L)

        // When
        val result = taskService.deleteTask(1L)

        // Then
        assertTrue(result)
        verify(taskRepository).existsById(1L)
        verify(taskRepository).deleteById(1L)
    }

    @Test
    fun `deleteTask should return false when task does not exist`() {
        // Given
        `when`(taskRepository.existsById(999L)).thenReturn(false)

        // When
        val result = taskService.deleteTask(999L)

        // Then
        assertFalse(result)
        verify(taskRepository).existsById(999L)
        verify(taskRepository, never()).deleteById(999L)
    }

    @Test
    fun `getTasksByStatus should return tasks with specified status`() {
        // Given
        val todoTasks = listOf(testTask)
        `when`(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(todoTasks)

        // When
        val result = taskService.getTasksByStatus(TaskStatus.TODO)

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskStatus.TODO, result[0].status)
        assertEquals("Test Task", result[0].title)
        verify(taskRepository).findByStatus(TaskStatus.TODO)
    }

    @Test
    fun `getTasksByStatus should return empty list when no tasks with status exist`() {
        // Given
        `when`(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(emptyList())

        // When
        val result = taskService.getTasksByStatus(TaskStatus.DONE)

        // Then
        assertTrue(result.isEmpty())
        verify(taskRepository).findByStatus(TaskStatus.DONE)
    }

    @Test
    fun `getTasksByStatus should return IN_REVIEW tasks`() {
        // Given
        val inReviewTask = Task(
            id = 1L,
            title = "Review Task",
            description = "Task in review",
            status = TaskStatus.IN_REVIEW,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(taskRepository.findByStatus(TaskStatus.IN_REVIEW)).thenReturn(listOf(inReviewTask))

        // When
        val result = taskService.getTasksByStatus(TaskStatus.IN_REVIEW)

        // Then
        assertEquals(1, result.size)
        assertEquals(TaskStatus.IN_REVIEW, result[0].status)
        assertEquals("Review Task", result[0].title)
        verify(taskRepository).findByStatus(TaskStatus.IN_REVIEW)
    }
}