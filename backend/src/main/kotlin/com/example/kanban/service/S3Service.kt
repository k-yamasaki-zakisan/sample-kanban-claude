package com.example.kanban.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.InputStream
import java.net.URI
import java.time.Duration
import java.util.*

@Service
@Profile("!test")
class S3Service(
    private val s3Client: S3Client,
    @Value("\${s3.bucket-name}")
    private val bucketName: String,
    @Value("\${s3.endpoint:}")
    private val endpoint: String
) : S3ServiceInterface {

    init {
        createBucketIfNotExists()
    }

    private fun createBucketIfNotExists() {
        try {
            val bucketExists = s3Client.headBucket(
                HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            )
        } catch (e: NoSuchBucketException) {
            // バケットが存在しない場合は作成
            s3Client.createBucket(
                CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            )
            
            // パブリック読み取り権限を設定（開発環境用）
            if (endpoint.isNotBlank()) {
                try {
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
                    
                    s3Client.putBucketPolicy(
                        PutBucketPolicyRequest.builder()
                            .bucket(bucketName)
                            .policy(policy)
                            .build()
                    )
                } catch (e: Exception) {
                    // ポリシー設定に失敗しても続行
                    println("Warning: Failed to set bucket policy: ${e.message}")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to verify/create bucket: ${e.message}", e)
        }
    }

    override fun uploadFile(file: MultipartFile, objectName: String): String {
        return try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .contentType(file.contentType)
                    .contentLength(file.size)
                    .build(),
                RequestBody.fromInputStream(file.inputStream, file.size)
            )
            objectName
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload file: ${e.message}", e)
        }
    }

    override fun uploadFile(inputStream: InputStream, objectName: String, contentType: String, size: Long): String {
        return try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .contentType(contentType)
                    .contentLength(size)
                    .build(),
                RequestBody.fromInputStream(inputStream, size)
            )
            objectName
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload file: ${e.message}", e)
        }
    }

    override fun downloadFile(objectName: String): InputStream {
        return try {
            val response = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build()
            )
            response
        } catch (e: Exception) {
            throw RuntimeException("Failed to download file: ${e.message}", e)
        }
    }

    override fun deleteFile(objectName: String): Boolean {
        return try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getFileUrl(objectName: String): String {
        return try {
            // プリサインドURLを生成（24時間有効）
            val presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .build()
            
            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24))
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .build()
                )
                .build()
            
            val presignedRequest = presigner.presignGetObject(presignRequest)
            presignedRequest.url().toString()
        } catch (e: Exception) {
            // フォールバック: 直接URLを返す（MinIO開発環境用）
            if (endpoint.isNotBlank()) {
                "$endpoint/$bucketName/$objectName"
            } else {
                "https://$bucketName.s3.amazonaws.com/$objectName"
            }
        }
    }

    override fun fileExists(objectName: String): Boolean {
        return try {
            s3Client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}