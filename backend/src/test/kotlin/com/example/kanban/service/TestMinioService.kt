package com.example.kanban.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
@Profile("test")
class TestMinioService : MinioServiceInterface {
    
    override fun uploadFile(file: MultipartFile, objectName: String): String {
        // テスト環境では実際のアップロードを行わず、ダミーURLを返す
        return "http://test-minio/test-bucket/$objectName"
    }
    
    override fun uploadFile(inputStream: InputStream, objectName: String, contentType: String, size: Long): String {
        // テスト環境では実際のアップロードを行わず、ダミーURLを返す
        return "http://test-minio/test-bucket/$objectName"
    }
    
    override fun deleteFile(objectName: String): Boolean {
        // テスト環境では常に成功とする
        return true
    }
    
    override fun getFileUrl(objectName: String): String {
        // テスト環境ではダミーURLを返す
        return "http://test-minio/test-bucket/$objectName"
    }
    
    override fun fileExists(objectName: String): Boolean {
        // テスト環境では常にfalseを返す（新規ファイルとして扱う）
        return false
    }
}