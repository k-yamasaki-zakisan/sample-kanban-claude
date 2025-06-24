package com.example.kanban.controller

import com.example.kanban.dto.TaskCreateDto
import com.example.kanban.dto.TaskUpdateDto
import com.example.kanban.model.TaskStatus
import com.example.kanban.service.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jwtService: JwtService

    private fun createTestToken(): String {
        return jwtService.generateToken("test@example.com", 1L)
    }

    @Test
    @Transactional
    fun `タスク作成時の空のタイトルでバリデーションエラーが返される`() {
        val taskCreateDto = TaskCreateDto(
            title = "",
            description = "テスト説明"
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'title' && @.message == 'タスクタイトルは必須です')]").exists())
    }

    @Test
    @Transactional
    fun `タスク作成時の長いタイトルでバリデーションエラーが返される`() {
        val longTitle = "a".repeat(201)  // 200文字以上のタイトル
        val taskCreateDto = TaskCreateDto(
            title = longTitle,
            description = "テスト説明"
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").value("タイトルは1文字以上200文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `タスク作成時の長い説明でバリデーションエラーが返される`() {
        val longDescription = "a".repeat(1001)  // 1000文字以上の説明
        val taskCreateDto = TaskCreateDto(
            title = "テストタスク",
            description = longDescription
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'description')].message").value("説明は1000文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `タスク作成時の複数のバリデーションエラーが同時に返される`() {
        val longTitle = "a".repeat(201)
        val longDescription = "b".repeat(1001)
        val taskCreateDto = TaskCreateDto(
            title = longTitle,
            description = longDescription
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(2))
    }

    @Test
    @Transactional
    fun `タスク作成時の有効な入力で成功する`() {
        val taskCreateDto = TaskCreateDto(
            title = "有効なタスクタイトル",
            description = "有効な説明文"
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("有効なタスクタイトル"))
            .andExpect(jsonPath("$.description").value("有効な説明文"))
    }

    @Test
    @Transactional
    fun `タスク更新時の長いタイトルでバリデーションエラーが返される`() {
        val longTitle = "a".repeat(201)
        val taskUpdateDto = TaskUpdateDto(
            title = longTitle,
            description = "更新された説明",
            status = TaskStatus.IN_PROGRESS
        )

        mockMvc.perform(
            put("/api/tasks/1")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").value("タイトルは1文字以上200文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `タスク更新時の長い説明でバリデーションエラーが返される`() {
        val longDescription = "a".repeat(1001)
        val taskUpdateDto = TaskUpdateDto(
            title = "更新されたタイトル",
            description = longDescription,
            status = TaskStatus.DONE
        )

        mockMvc.perform(
            put("/api/tasks/1")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'description')].message").value("説明は1000文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `認証なしでタスク作成すると403が返される`() {
        val taskCreateDto = TaskCreateDto(
            title = "認証なしタスク",
            description = "認証なし説明"
        )

        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @Transactional
    fun `無効なJWTトークンでタスク作成すると403が返される`() {
        val taskCreateDto = TaskCreateDto(
            title = "無効トークンタスク",
            description = "無効トークン説明"
        )

        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDto))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @Transactional
    fun `タスク作成時の空のリクエストボディでJSONパースエラーが返される`() {
        mockMvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer ${createTestToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("リクエストの形式が正しくありません"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0].field").value("request"))
            .andExpect(jsonPath("$.errors[0].message").value("必須項目が不足しているか、形式が正しくありません"))
    }
}