package com.example.kanban.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    private val secretKey: SecretKey by lazy {
        val secret = System.getenv("JWT_SECRET") 
            ?: "mySecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm"
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val jwtExpiration: Long = 
        System.getenv("JWT_EXPIRATION_MS")?.toLongOrNull() ?: 3600000 // 1 hour

    fun generateToken(email: String, userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extractEmail(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims.subject
        } catch (e: Exception) {
            null
        }
    }

    fun extractUserId(token: String): Long? {
        return try {
            val claims = extractAllClaims(token)
            claims.get("userId", Long::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}