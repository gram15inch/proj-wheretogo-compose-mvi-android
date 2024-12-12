package com.wheretogo.domain.repository

interface ImageRepository {
    suspend fun getImage(fileName: String, size: String): String
}