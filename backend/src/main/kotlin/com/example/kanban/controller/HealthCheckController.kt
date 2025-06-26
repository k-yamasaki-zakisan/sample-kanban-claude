package com.example.kanban.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class HealthCheckController {

    @GetMapping("/healthcheck")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val response = mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now(),
            "service" to "Kanban Backend - 純正DevTools使用",
            "version" to "1.0.19",
            "message" to "Spring Boot純正ホットリロード実装完了",
            "reloadTime" to System.currentTimeMillis(),
            "devtools" to "pure-spring-boot"
        )
        return ResponseEntity.ok(response)
    }
}