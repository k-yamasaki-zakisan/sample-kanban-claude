package com.example.kanban.controller

import com.example.kanban.dto.LoginRequestDto
import com.example.kanban.dto.UserCreateDto
import com.example.kanban.dto.UserUpdateDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
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
class AuthControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @Transactional
    fun `ログイン時の無効なメールアドレス形式でバリデーションエラーが返される`() {
        val loginRequest = LoginRequestDto(
            email = "invalid-email",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("正しいメールアドレス形式で入力してください"))
    }

    @Test
    @Transactional
    fun `ログイン時の短いパスワードでバリデーションエラーが返される`() {
        val loginRequest = LoginRequestDto(
            email = "test@example.com",
            password = "123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'password')].message").value("パスワードは6文字以上100文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `ログイン時の複数のバリデーションエラーが同時に返される`() {
        val loginRequest = LoginRequestDto(
            email = "invalid-email",
            password = "123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("正しいメールアドレス形式で入力してください"))
            .andExpect(jsonPath("$.errors[?(@.field == 'password')].message").value("パスワードは6文字以上100文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `ログイン時の必須フィールド不足でJSONパースエラーが返される`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("リクエストの形式が正しくありません"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0].field").value("request"))
            .andExpect(jsonPath("$.errors[0].message").value("必須項目が不足しているか、形式が正しくありません"))
    }

    @Test
    @Transactional
    fun `ユーザー登録時の無効な入力でバリデーションエラーが返される`() {
        val userCreateDto = UserCreateDto(
            name = "",  // 空の名前
            email = "invalid-email",  // 無効なメール
            password = "123"  // 短いパスワード
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(5))  // 5つのエラー（name, email, password x 3）
    }

    @Test
    @Transactional
    fun `ユーザー登録時の長いメールアドレスでバリデーションエラーが返される`() {
        val longEmail = "a".repeat(250) + "@example.com"  // 255文字以上のメール
        val userCreateDto = UserCreateDto(
            name = "テストユーザー",
            email = longEmail,
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'メールアドレスは255文字以内で入力してください')]").exists())
    }

    @Test
    @Transactional
    fun `ユーザー登録時の無効なパスワード形式でバリデーションエラーが返される`() {
        val userCreateDto = UserCreateDto(
            name = "テストユーザー",
            email = "test@example.com",
            password = "password"  // 数字を含まないパスワード
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'password')].message").value("パスワードは英数字を含む6文字以上で入力してください"))
    }

    @Test
    @Transactional
    fun `有効なログイン情報では認証失敗で401が返される`() {
        val loginRequest = LoginRequestDto(
            email = "nonexistent@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@example.com")
    fun `プロフィール更新時の無効な名前でバリデーションエラーが返される`() {
        val userUpdateDto = UserUpdateDto(
            name = "",  // 空の名前
            email = "updated@example.com"
        )

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").value("ユーザー名は1文字以上100文字以内で入力してください"))
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@example.com")
    fun `プロフィール更新時の無効なメールアドレス形式でバリデーションエラーが返される`() {
        val userUpdateDto = UserUpdateDto(
            name = "更新されたユーザー",
            email = "invalid-email"  // 無効なメール形式
        )

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("正しいメールアドレス形式で入力してください"))
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@example.com")
    fun `プロフィール更新時の長いメールアドレスでバリデーションエラーが返される`() {
        val longEmail = "a".repeat(250) + "@example.com"  // 255文字以上のメール
        val userUpdateDto = UserUpdateDto(
            name = "更新されたユーザー",
            email = longEmail
        )

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'メールアドレスは255文字以内で入力してください')]").exists())
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@example.com")
    fun `プロフィール更新時の長い名前でバリデーションエラーが返される`() {
        val longName = "a".repeat(101)  // 100文字以上の名前
        val userUpdateDto = UserUpdateDto(
            name = longName,
            email = "updated@example.com"
        )

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").value("ユーザー名は1文字以上100文字以内で入力してください"))
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@example.com")
    fun `プロフィール更新時の複数のバリデーションエラーが同時に返される`() {
        val userUpdateDto = UserUpdateDto(
            name = "",  // 空の名前
            email = "invalid-email"  // 無効なメール形式
        )

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").value("ユーザー名は1文字以上100文字以内で入力してください"))
            .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("正しいメールアドレス形式で入力してください"))
    }
}