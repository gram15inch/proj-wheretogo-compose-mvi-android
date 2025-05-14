package com.wheretogo.data.datasource

import com.wheretogo.data.LikeObject
import com.wheretogo.data.model.course.RemoteLike

interface LikeRemoteDatasource {

    suspend fun getLikeInObject(type: LikeObject, objectId: String): RemoteLike

    suspend fun setLikeInObject(type: LikeObject, objectId: String, remoteLike: RemoteLike): Boolean

    suspend fun removeLikeInCourse(type: LikeObject, objectId: String): Boolean

    fun getLikeId(objectId: String): String
}