package com.example.kanban.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "無効な値です"
            )
        }
        
        val response = ValidationErrorResponse(
            message = "入力値にエラーがあります",
            errors = errors
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<ValidationErrorResponse> {
        val response = ValidationErrorResponse(
            message = "リクエストの形式が正しくありません",
            errors = listOf(ValidationError(
                field = "request",
                message = "必須項目が不足しているか、形式が正しくありません"
            ))
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}

data class ValidationErrorResponse(
    val message: String,
    val errors: List<ValidationError>
)

data class ValidationError(
    val field: String,
    val message: String
)