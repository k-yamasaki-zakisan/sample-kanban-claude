package com.example.kanban.service

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface MinioServiceInterface {
    fun uploadFile(file: MultipartFile, objectName: String): String
    fun uploadFile(inputStream: InputStream, objectName: String, contentType: String, size: Long): String
    fun deleteFile(objectName: String): Boolean
    fun getFileUrl(objectName: String): String
    fun fileExists(objectName: String): Boolean
    fun downloadFile(objectName: String): InputStream
}