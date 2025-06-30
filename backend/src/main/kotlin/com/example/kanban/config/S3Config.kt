package com.example.kanban.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
@Profile("!test")
class S3Config {

    @Bean
    fun s3Client(
        @Value("\${s3.endpoint:}") endpoint: String,
        @Value("\${s3.region:us-east-1}") region: String,
        @Value("\${s3.access-key}") accessKey: String,
        @Value("\${s3.secret-key}") secretKey: String,
        @Value("\${s3.path-style-access:false}") pathStyleAccess: Boolean
    ): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        val credentialsProvider = StaticCredentialsProvider.create(credentials)
        
        val clientBuilder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(pathStyleAccess)
                    .build()
            )
        
        // MinIO用のエンドポイントが設定されている場合
        if (endpoint.isNotBlank()) {
            clientBuilder.endpointOverride(URI.create(endpoint))
        }
        
        return clientBuilder.build()
    }
}