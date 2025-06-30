package com.example.kanban.repository

import com.example.kanban.model.TaskImage

interface TaskImageRepository {
    fun findByUserId(userId: Long): List<TaskImage>
    fun findByUserIdOrderByUploadOrder(userId: Long): List<TaskImage>
    fun findByUserIdAndIsTemporary(userId: Long, isTemporary: Boolean): List<TaskImage>
    fun findById(id: Long): TaskImage?
    fun findByIdAndUserId(id: Long, userId: Long): TaskImage?
    fun save(taskImage: TaskImage): TaskImage
    fun deleteById(id: Long)
    fun deleteByUserId(userId: Long)
    fun existsById(id: Long): Boolean
    fun existsByIdAndUserId(id: Long, userId: Long): Boolean
    fun markAsNonTemporary(imageIds: List<Long>, userId: Long)
    fun deleteTemporaryImagesByUserId(userId: Long)
}