package com.example.kanban.service

import com.example.kanban.dto.LoginRequestDto
import com.example.kanban.dto.UserCreateDto
import com.example.kanban.dto.UserResponseDto
import com.example.kanban.model.User
import com.example.kanban.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var jwtService: JwtService

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private lateinit var testUserCreateDto: UserCreateDto
    private lateinit var testLoginRequest: LoginRequestDto
    private val testUserId = 1L
    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testName = "Test User"
    private val passwordEncoder = BCryptPasswordEncoder(10)

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = testUserId,
            name = testName,
            email = testEmail,
            password = passwordEncoder.encode(testPassword),
            lastLogin = null,
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1)
        )

        testUserCreateDto = UserCreateDto(
            name = testName,
            email = testEmail,
            password = testPassword
        )

        testLoginRequest = LoginRequestDto(
            email = testEmail,
            password = testPassword
        )
    }

    @Test
    fun `findByEmail should return user when exists`() {
        // Given
        `when`(userRepository.findByEmail(testEmail)).thenReturn(testUser)

        // When
        val result = userService.findByEmail(testEmail)

        // Then
        assertNotNull(result)
        assertEquals(testUserId, result!!.id)
        assertEquals(testName, result.name)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `findByEmail should return null when user does not exist`() {
        // Given
        `when`(userRepository.findByEmail(testEmail)).thenReturn(null)

        // When
        val result = userService.findByEmail(testEmail)

        // Then
        assertNull(result)
    }

    @Test
    fun `findById should return user when exists`() {
        // Given
        `when`(userRepository.findById(testUserId)).thenReturn(testUser)

        // When
        val result = userService.findById(testUserId)

        // Then
        assertNotNull(result)
        assertEquals(testUserId, result!!.id)
        assertEquals(testName, result.name)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `findById should return null when user does not exist`() {
        // Given
        `when`(userRepository.findById(testUserId)).thenReturn(null)

        // When
        val result = userService.findById(testUserId)

        // Then
        assertNull(result)
    }

    @Test
    fun `createUser should create user when email does not exist`() {
        // Given
        `when`(userRepository.existsByEmail(testEmail)).thenReturn(false)
        `when`(userRepository.save(any())).thenReturn(testUser)

        // When
        val result = userService.createUser(testUserCreateDto)

        // Then
        assertEquals(testUserId, result.id)
        assertEquals(testName, result.name)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `createUser should throw exception when email already exists`() {
        // Given
        `when`(userRepository.existsByEmail(testEmail)).thenReturn(true)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            userService.createUser(testUserCreateDto)
        }
        
        assertEquals("Email already exists", exception.message)
    }

    @Test
    fun `authenticate should return user when credentials are valid`() {
        // Given
        `when`(userRepository.findByEmail(testEmail)).thenReturn(testUser)
        `when`(userRepository.updateLastLogin(any(), any(), any())).thenReturn(true)

        // When
        val result = userService.authenticate(testLoginRequest)

        // Then
        assertNotNull(result)
        assertEquals(testUserId, result!!.id)
        assertEquals(testName, result.name)
        assertEquals(testEmail, result.email)
    }

    @Test
    fun `authenticate should return null when user does not exist`() {
        // Given
        `when`(userRepository.findByEmail(testEmail)).thenReturn(null)

        // When
        val result = userService.authenticate(testLoginRequest)

        // Then
        assertNull(result)
    }

    @Test
    fun `authenticate should return null when password is incorrect`() {
        // Given
        val wrongPasswordRequest = LoginRequestDto(
            email = testEmail,
            password = "wrongpassword"
        )
        `when`(userRepository.findByEmail(testEmail)).thenReturn(testUser)

        // When
        val result = userService.authenticate(wrongPasswordRequest)

        // Then
        assertNull(result)
    }
}