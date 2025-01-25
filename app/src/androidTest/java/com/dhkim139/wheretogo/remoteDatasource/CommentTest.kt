package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.model.dummy.getCommentDummy
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class CommentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var commentRemoteDatasourceImpl: CommentRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun setCommentTest(): Unit = runBlocking {
        val datasource = commentRemoteDatasourceImpl
        val comment = RemoteComment(
            commentId = "cm11",
            commentGroupId = "cp10",
            oneLineReview = "hi"
        )
        datasource.setCommentInCheckPoint(comment, true)
    }

    @Test
    fun removeCommentTest(): Unit = runBlocking {
        val datasource = commentRemoteDatasourceImpl
        val comment = RemoteComment(
            commentId = "cm11",
            commentGroupId = "cp10",
            oneLineReview = "hi"
        )
        datasource.removeCommentInCheckPoint(comment)
    }

    @Test
    fun setCommentGroupTest(): Unit = runBlocking {
        val datasource = commentRemoteDatasourceImpl

        val list = (1..12).map {
            RemoteCommentGroupWrapper("cp$it",
                getCommentDummy("cp$it").map {
                    it.toRemoteComment().copy(timestamp = System.currentTimeMillis())
                }
            )
        }
        list.forEach {
            assertEquals(true, datasource.setCommentGroupInCheckPoint(it))
        }
    }


    @Test
    fun getCommentTest(): Unit = runBlocking {
        val datasource = commentRemoteDatasourceImpl

        val list = (1..12).map {
            RemoteCommentGroupWrapper("cp$it",
                getCommentDummy("cp$it").map {
                    it.toRemoteComment()
                }
            )
        }

        list.forEach {
            assertEquals(it.groupId, datasource.getCommentGroupInCheckPoint(it.groupId)?.groupId)
            assertEquals(
                6,
                datasource.getCommentGroupInCheckPoint(it.groupId)?.remoteCommentGroup?.size
            )
        }
    }
}