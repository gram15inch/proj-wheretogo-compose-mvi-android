package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl@Inject constructor(
    private val imageLocalDatasource: ImageLocalDatasource,

    ):ImageRepository {
    override suspend fun getImage(fileName: String, size: String): String{
        return imageLocalDatasource.getImage(fileName,size)
    }
}