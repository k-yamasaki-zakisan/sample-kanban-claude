package com.example.kanban.repository

import com.example.kanban.model.TaskImage
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class TaskImageRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : TaskImageRepository {

    override fun findByUserId(userId: Long): List<TaskImage> {
        return entityManager.createQuery(
            "SELECT ti FROM TaskImage ti WHERE ti.userId = :userId ORDER BY ti.uploadOrder ASC",
            TaskImage::class.java
        ).setParameter("userId", userId)
            .resultList
    }

    override fun findByUserIdOrderByUploadOrder(userId: Long): List<TaskImage> {
        return findByUserId(userId)
    }

    override fun findByUserIdAndIsTemporary(userId: Long, isTemporary: Boolean): List<TaskImage> {
        return entityManager.createQuery(
            "SELECT ti FROM TaskImage ti WHERE ti.userId = :userId AND ti.isTemporary = :isTemporary ORDER BY ti.uploadOrder ASC",
            TaskImage::class.java
        ).setParameter("userId", userId)
            .setParameter("isTemporary", isTemporary)
            .resultList
    }

    override fun findById(id: Long): TaskImage? {
        return try {
            entityManager.find(TaskImage::class.java, id)
        } catch (e: Exception) {
            null
        }
    }

    override fun findByIdAndUserId(id: Long, userId: Long): TaskImage? {
        return try {
            entityManager.createQuery(
                "SELECT ti FROM TaskImage ti WHERE ti.id = :id AND ti.userId = :userId",
                TaskImage::class.java
            ).setParameter("id", id)
                .setParameter("userId", userId)
                .singleResult
        } catch (e: Exception) {
            null
        }
    }

    @Transactional
    override fun save(taskImage: TaskImage): TaskImage {
        return if (taskImage.id == 0L) {
            val newImage = taskImage.copy(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            entityManager.persist(newImage)
            entityManager.flush()
            newImage
        } else {
            val updatedImage = taskImage.copy(updatedAt = LocalDateTime.now())
            entityManager.merge(updatedImage)
        }
    }

    @Transactional
    override fun deleteById(id: Long) {
        val taskImage = findById(id)
        if (taskImage != null) {
            entityManager.remove(taskImage)
        }
    }

    @Transactional
    override fun deleteByUserId(userId: Long) {
        entityManager.createQuery("DELETE FROM TaskImage ti WHERE ti.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate()
    }

    override fun existsById(id: Long): Boolean {
        return entityManager.createQuery(
            "SELECT COUNT(ti) FROM TaskImage ti WHERE ti.id = :id",
            Long::class.java
        ).setParameter("id", id)
            .singleResult > 0
    }

    override fun existsByIdAndUserId(id: Long, userId: Long): Boolean {
        return entityManager.createQuery(
            "SELECT COUNT(ti) FROM TaskImage ti WHERE ti.id = :id AND ti.userId = :userId",
            Long::class.java
        ).setParameter("id", id)
            .setParameter("userId", userId)
            .singleResult > 0
    }

    @Transactional
    override fun markAsNonTemporary(imageIds: List<Long>, userId: Long) {
        if (imageIds.isNotEmpty()) {
            entityManager.createQuery(
                "UPDATE TaskImage ti SET ti.isTemporary = false, ti.updatedAt = :now WHERE ti.id IN :ids AND ti.userId = :userId"
            ).setParameter("ids", imageIds)
                .setParameter("userId", userId)
                .setParameter("now", LocalDateTime.now())
                .executeUpdate()
        }
    }

    @Transactional
    override fun deleteTemporaryImagesByUserId(userId: Long) {
        entityManager.createQuery("DELETE FROM TaskImage ti WHERE ti.userId = :userId AND ti.isTemporary = true")
            .setParameter("userId", userId)
            .executeUpdate()
    }
}