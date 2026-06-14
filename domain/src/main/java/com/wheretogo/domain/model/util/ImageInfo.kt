package com.wheretogo.domain.model.util

data class ImageInfo(val uriString: String?, val fileName: String, val byte: Long){
    fun isValid(): Boolean{
        return try {
            require(uriString!=null)
            true
        }catch (e: Exception){
            false
        }
    }
}