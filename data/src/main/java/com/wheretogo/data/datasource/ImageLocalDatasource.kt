package com.wheretogo.data.datasource


interface ImageLocalDatasource {

    suspend fun getImage(remotePath: String, size: String): String

}