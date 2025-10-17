package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.content.ContentLike
import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.firebase.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContentApiService {
    @POST("api/content/comment/like")
    suspend fun like(
        @Body body: ContentLike
    ): Response<MessageResponse>

    @GET("api/content/comment")
    suspend fun getCommentByGroup(
        @Query("groupId") groupId: String,
    ): Response<DataResponse<List<RemoteComment>>>

    @POST("api/content/comment")
    suspend fun addComment(
        @Body body: RemoteComment
    ): Response<MessageResponse>

    @DELETE("api/content/comment")
    suspend fun removeComment(
        @Query("groupId") groupId: String,
        @Query("contentId") contentId: String
    ): Response<MessageResponse>
}