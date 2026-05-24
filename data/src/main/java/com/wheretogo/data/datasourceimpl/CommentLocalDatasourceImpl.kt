package com.wheretogo.data.datasourceimpl


import com.wheretogo.data.datasource.CommentLocalDatasource
import com.wheretogo.data.model.comment.CacheCommentGroupWrapper
import com.wheretogo.domain.model.comment.Comment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class CommentLocalDatasourceImpl @Inject constructor() : CommentLocalDatasource {
    private val _mutex = Mutex()
    private val _cacheByGroupId = mutableMapOf<String, CacheCommentGroupWrapper>() // 체크포인트 그룹id

    override suspend fun safeReplace(
        groupId: String,
        commentGroup: List<Comment>?,
        isRefresh: Boolean
    ) = _mutex.withLock {
        _cacheByGroupId.safeReplace(groupId, commentGroup, isRefresh)
    }

    override suspend fun get(groupId: String): CacheCommentGroupWrapper? {
        return _mutex.withLock {
            _cacheByGroupId.getOrDefault(groupId, null)
        }
    }

    override suspend fun safeGet(groupId: String): CacheCommentGroupWrapper = _mutex.withLock {
        return _cacheByGroupId.safeGet(groupId)
    }

    override suspend fun clear() = _mutex.withLock {
        _cacheByGroupId.clear()
    }

    private fun MutableMap<String, CacheCommentGroupWrapper>.safeGet(groupId: String): CacheCommentGroupWrapper {
        return getOrPut(groupId) { createWrapper() }
    }

    private fun MutableMap<String, CacheCommentGroupWrapper>.safeReplace(
        groupId: String,
        commentGroup: List<Comment>? = null,
        isRefresh: Boolean = false
    ) {
        val timestamp = if (isRefresh) System.currentTimeMillis() else safeGet(groupId).updatedAt
        put(
            groupId, CacheCommentGroupWrapper(
                commentGroup = commentGroup ?: emptyList(),
                updatedAt = timestamp
            )
        )
    }

    private fun createWrapper(commentGroup: List<Comment>? = null): CacheCommentGroupWrapper {
        val timestamp = if (commentGroup != null) System.currentTimeMillis() else 0
        return CacheCommentGroupWrapper(
            commentGroup = commentGroup ?: emptyList(),
            updatedAt = timestamp
        )
    }
}

