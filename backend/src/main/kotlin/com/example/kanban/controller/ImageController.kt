package com.example.kanban.controller

import com.example.kanban.dto.TaskImageDto
import com.example.kanban.service.TaskImageService
import com.example.kanban.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = ["http://localhost:3000"])
class ImageController(
    private val taskImageService: TaskImageService,
    private val userService: UserService
) {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadImage(
        @RequestParam("file") file: MultipartFile,
        request: HttpServletRequest
    ): ResponseEntity<TaskImageDto> {
        return try {
            val userId = userService.getCurrentUserId(request)
            val imageDto = taskImageService.uploadTemporaryImage(file, userId)
            ResponseEntity.ok(imageDto)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/user")
    fun getUserImages(
        @RequestParam("temporary", defaultValue = "false") onlyTemporary: Boolean,
        request: HttpServletRequest
    ): ResponseEntity<List<TaskImageDto>> {
        return try {
            val userId = userService.getCurrentUserId(request)
            val images = if (onlyTemporary) {
                taskImageService.getTemporaryImages(userId)
            } else {
                taskImageService.getUserImages(userId, includeTemporary = true)
            }
            ResponseEntity.ok(images)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{imageId}")
    fun getImage(
        @PathVariable imageId: Long,
        request: HttpServletRequest
    ): ResponseEntity<ByteArray> {
        return try {
            val userId = userService.getCurrentUserId(request)
            val (data, contentType) = taskImageService.getImageData(imageId, userId)
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(data)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{imageId}")
    fun deleteImage(
        @PathVariable imageId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        return try {
            val userId = userService.getCurrentUserId(request)
            taskImageService.deleteImage(imageId, userId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/temporary")
    fun deleteTemporaryImages(
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        return try {
            val userId = userService.getCurrentUserId(request)
            taskImageService.deleteTemporaryImages(userId)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}