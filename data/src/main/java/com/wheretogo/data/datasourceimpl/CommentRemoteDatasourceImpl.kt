package com.wheretogo.data.datasourceimpl


import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.ContentApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.content.ContentLike
import com.wheretogo.data.toDataError
import javax.inject.Inject

class CommentRemoteDatasourceImpl @Inject constructor(
    private val contentApiService: ContentApiService
) : CommentRemoteDatasource {
    override suspend fun getCommentByGroupId(groupId: String): Result<List<RemoteComment>> {
        return dataErrorCatching { contentApiService.getCommentByGroup(groupId) }
            .mapSuccess {
                if (!it.isSuccessful)
                    Result.failure(it.toDataError())
                else
                    Result.success(it.body()?.data ?: emptyList())
            }
    }

    override suspend fun addComment(comment: RemoteComment): Result<Unit> {
        return dataErrorCatching { contentApiService.addComment(comment) }
            .mapSuccess {
                if (!it.isSuccessful)
                    Result.failure(it.toDataError())
                else
                    Result.success(Unit)
            }
    }

    override suspend fun removeComment(groupId: String, contentId: String): Result<Unit> {
        return dataErrorCatching { contentApiService.removeComment(groupId, contentId) }
            .mapSuccess {
                if (!it.isSuccessful)
                    Result.failure(it.toDataError())
                else
                    Result.success(Unit)
            }
    }

    override suspend fun changeCommentLike(request: ContentLike): Result<Unit> {
        return dataErrorCatching { contentApiService.like(request) }
            .mapSuccess {
                if (!it.isSuccessful)
                    Result.failure(it.toDataError())
                else
                    Result.success(Unit)
            }
    }
}

