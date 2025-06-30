package com.example.kanban.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "task_images")
data class TaskImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "filename", nullable = false)
    val filename: String,

    @Column(name = "original_filename", nullable = false)
    val originalFilename: String,

    @Column(name = "content_type", nullable = false)
    val contentType: String,

    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    @Column(name = "minio_bucket", nullable = false)
    val minioBucket: String = "kanban-images",

    @Column(name = "minio_object_key", nullable = false)
    val minioObjectKey: String,

    @Column(name = "upload_order", nullable = false)
    val uploadOrder: Int = 0,

    @Column(name = "is_temporary", nullable = false)
    val isTemporary: Boolean = true,

    @Column(name = "description_content", columnDefinition = "TEXT")
    val descriptionContent: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)