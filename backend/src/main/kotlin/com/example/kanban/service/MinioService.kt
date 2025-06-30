package com.example.kanban.service

import io.minio.*
import io.minio.errors.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*

@Service
class MinioService(
    @Value("\${minio.endpoint:http://localhost:9000}")
    private val endpoint: String,
    
    @Value("\${minio.access-key:minio_user}")
    private val accessKey: String,
    
    @Value("\${minio.secret-key:minio_password123}")
    private val secretKey: String,
    
    @Value("\${minio.bucket-name:kanban-images}")
    private val bucketName: String
) {
    
    private val minioClient: MinioClient by lazy {
        MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }

    init {
        createBucketIfNotExists()
    }

    private fun createBucketIfNotExists() {
        try {
            val bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )
            
            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )
                
                // Set public read policy for the bucket
                val policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::$bucketName/*"]
                            }
                        ]
                    }
                """.trimIndent()
                
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to create bucket: ${e.message}", e)
        }
    }

    fun uploadFile(file: MultipartFile, objectName: String): String {
        return try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType)
                    .build()
            )
            objectName
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload file: ${e.message}", e)
        }
    }

    fun uploadFile(inputStream: InputStream, objectName: String, contentType: String, size: Long): String {
        return try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            )
            objectName
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload file: ${e.message}", e)
        }
    }

    fun downloadFile(objectName: String): InputStream {
        return try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to download file: ${e.message}", e)
        }
    }

    fun deleteFile(objectName: String) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete file: ${e.message}", e)
        }
    }

    fun generateFileName(originalFilename: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        val extension = originalFilename.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            "${timestamp}_${uuid}.${extension}"
        } else {
            "${timestamp}_${uuid}"
        }
    }

    fun getFileUrl(objectName: String): String {
        return try {
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .`object`(objectName)
                    .expiry(60 * 60 * 24) // 24 hours
                    .build()
            )
        } catch (e: Exception) {
            // Fallback to direct URL if presigned URL fails
            "$endpoint/$bucketName/$objectName"
        }
    }

    fun getPublicUrl(objectName: String): String {
        return "$endpoint/$bucketName/$objectName"
    }
}