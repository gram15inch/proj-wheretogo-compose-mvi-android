package com.wheretogo.data.datasource


interface ImageLocalDatasource {

    suspend fun setCheckPointImage(remotePath: String): String

}