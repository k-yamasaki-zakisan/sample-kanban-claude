package com.example.kanban.service

import com.example.kanban.dto.TaskImageDto
import com.example.kanban.model.TaskImage
import com.example.kanban.repository.TaskImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.regex.Pattern

@Service
class TaskImageService(
    private val taskImageRepository: TaskImageRepository,
    private val minioService: MinioService
) {

    @Transactional
    fun uploadTemporaryImage(file: MultipartFile, userId: Long): TaskImageDto {
        // Validate file
        validateImageFile(file)

        // Generate unique filename
        val filename = minioService.generateFileName(file.originalFilename ?: "image")
        
        // Upload to MinIO
        val objectKey = minioService.uploadFile(file, filename)

        // Save to database as temporary
        val taskImage = TaskImage(
            userId = userId,
            filename = filename,
            originalFilename = file.originalFilename ?: "image",
            contentType = file.contentType ?: "application/octet-stream",
            fileSize = file.size,
            minioObjectKey = objectKey,
            uploadOrder = 0,
            isTemporary = true
        )

        val savedImage = taskImageRepository.save(taskImage)
        return toDto(savedImage)
    }

    fun getUserImages(userId: Long, includeTemporary: Boolean = true): List<TaskImageDto> {
        return if (includeTemporary) {
            taskImageRepository.findByUserId(userId)
        } else {
            taskImageRepository.findByUserIdAndIsTemporary(userId, false)
        }.map { toDto(it) }
    }

    fun getTemporaryImages(userId: Long): List<TaskImageDto> {
        return taskImageRepository.findByUserIdAndIsTemporary(userId, true)
            .map { toDto(it) }
    }

    @Transactional
    fun markImagesAsUsed(imageIds: List<Long>, userId: Long, descriptionContent: String) {
        // Mark images as non-temporary
        taskImageRepository.markAsNonTemporary(imageIds, userId)
        
        // Update description content for all images
        imageIds.forEach { imageId ->
            val image = taskImageRepository.findByIdAndUserId(imageId, userId)
            if (image != null) {
                val updatedImage = image.copy(
                    isTemporary = false,
                    descriptionContent = descriptionContent
                )
                taskImageRepository.save(updatedImage)
            }
        }
    }

    @Transactional
    fun deleteImage(imageId: Long, userId: Long) {
        val image = taskImageRepository.findByIdAndUserId(imageId, userId)
            ?: throw IllegalArgumentException("Image not found or access denied")

        // Delete from MinIO
        try {
            minioService.deleteFile(image.minioObjectKey)
        } catch (e: Exception) {
            // Log error but continue with database deletion
            println("Failed to delete file from MinIO: ${e.message}")
        }

        // Delete from database
        taskImageRepository.deleteById(imageId)
    }

    @Transactional
    fun deleteTemporaryImages(userId: Long) {
        val temporaryImages = taskImageRepository.findByUserIdAndIsTemporary(userId, true)
        
        // Delete files from MinIO
        temporaryImages.forEach { image ->
            try {
                minioService.deleteFile(image.minioObjectKey)
            } catch (e: Exception) {
                // Log error but continue
                println("Failed to delete file from MinIO: ${e.message}")
            }
        }

        // Delete from database
        taskImageRepository.deleteTemporaryImagesByUserId(userId)
    }

    fun getImageData(imageId: Long, userId: Long): Pair<ByteArray, String> {
        val image = taskImageRepository.findByIdAndUserId(imageId, userId)
            ?: throw IllegalArgumentException("Image not found or access denied")

        val inputStream = minioService.downloadFile(image.minioObjectKey)
        val data = inputStream.readAllBytes()
        return Pair(data, image.contentType)
    }

    fun extractImageIdsFromMarkdown(markdownContent: String): List<Long> {
        val pattern = Pattern.compile("!\\[[^\\]]*\\]\\([^)]*/(\\d+)\\)")
        val matcher = pattern.matcher(markdownContent)
        val imageIds = mutableListOf<Long>()
        
        while (matcher.find()) {
            try {
                val imageId = matcher.group(1).toLong()
                imageIds.add(imageId)
            } catch (e: NumberFormatException) {
                // Ignore invalid image IDs
            }
        }
        
        return imageIds
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }

        val maxSize = 10 * 1024 * 1024 // 10MB
        if (file.size > maxSize) {
            throw IllegalArgumentException("File size exceeds maximum allowed size (10MB)")
        }

        val allowedTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("File type not allowed. Only JPEG, PNG, GIF, and WebP images are allowed")
        }
    }

    private fun toDto(taskImage: TaskImage): TaskImageDto {
        return TaskImageDto(
            id = taskImage.id,
            filename = taskImage.filename,
            originalFilename = taskImage.originalFilename,
            contentType = taskImage.contentType,
            fileSize = taskImage.fileSize,
            uploadOrder = taskImage.uploadOrder,
            imageUrl = minioService.getPublicUrl(taskImage.minioObjectKey),
            createdAt = taskImage.createdAt
        )
    }
}