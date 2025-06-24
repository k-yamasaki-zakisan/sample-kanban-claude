package com.example.kanban.exception

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @Transactional
    fun `バリデーションエラーが適切にハンドリングされる`() {
        val invalidLoginRequest = mapOf(
            "email" to "invalid-email",
            "password" to "123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("正しいメールアドレス形式で入力してください"))
            .andExpect(jsonPath("$.errors[?(@.field == 'password')].message").value("パスワードは6文字以上100文字以内で入力してください"))
    }

    @Test
    @Transactional
    fun `JSONパースエラーが適切にハンドリングされる`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}") // 必須フィールドが不足
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("リクエストの形式が正しくありません"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("request"))
            .andExpect(jsonPath("$.errors[0].message").value("必須項目が不足しているか、形式が正しくありません"))
    }

    @Test
    @Transactional
    fun `不正なJSONフォーマットでJSONパースエラーが返される`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}") // 不正なJSON
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("リクエストの形式が正しくありません"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0].field").value("request"))
            .andExpect(jsonPath("$.errors[0].message").value("必須項目が不足しているか、形式が正しくありません"))
    }

    @Test
    @Transactional
    fun `空のリクエストボディでJSONパースエラーが返される`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("") // 空のボディ
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("リクエストの形式が正しくありません"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0].field").value("request"))
            .andExpect(jsonPath("$.errors[0].message").value("必須項目が不足しているか、形式が正しくありません"))
    }

    @Test
    @Transactional
    fun `複数フィールドのバリデーションエラーが全て含まれる`() {
        val invalidUserRequest = mapOf(
            "name" to "", // 空の名前
            "email" to "invalid-email", // 無効なメール
            "password" to "123" // 短いパスワード
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors.length()").value(5)) // name(2) + email(1) + password(2) = 5
            .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists())
    }

    @Test
    @Transactional
    fun `ValidationErrorResponseの構造が正しい`() {
        val invalidRequest = mapOf(
            "email" to "invalid",
            "password" to "123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").isString)
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0].field").exists())
            .andExpect(jsonPath("$.errors[0].field").isString)
            .andExpect(jsonPath("$.errors[0].message").exists())
            .andExpect(jsonPath("$.errors[0].message").isString)
    }

    @Test
    @Transactional
    fun `エラーレスポンスにUnknownプロパティが含まれない`() {
        val invalidRequest = mapOf(
            "email" to "invalid-email",
            "password" to "123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").doesNotExist()) // Spring Bootデフォルトエラー属性が含まれていない
            .andExpect(jsonPath("$.status").doesNotExist())
            .andExpect(jsonPath("$.error").doesNotExist())
            .andExpect(jsonPath("$.path").doesNotExist())
    }
}