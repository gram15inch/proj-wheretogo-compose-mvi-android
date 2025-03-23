package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class CommentTest {
    val tag = "tst_comment"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var commentRemoteDatasourceImpl: CommentRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun get_set_remove_should_work_correctly(): Unit = runBlocking {
        val comment = RemoteComment(
            commentId = "test_cm1",
            commentGroupId = "test_cm_gp1",
            oneLineReview = "hi"
        )
        commentRemoteDatasourceImpl.removeCommentGroupInCheckPoint(comment.commentGroupId)
        commentRemoteDatasourceImpl.getCommentGroupInCheckPoint(comment.commentGroupId).empty()

        commentRemoteDatasourceImpl.setCommentInCheckPoint(comment, true)
        commentRemoteDatasourceImpl.getCommentGroupInCheckPoint(comment.commentGroupId)!!.remoteCommentGroup.first()
            .assertEquals(comment)

        commentRemoteDatasourceImpl.removeCommentGroupInCheckPoint(comment.commentGroupId)
        commentRemoteDatasourceImpl.getCommentGroupInCheckPoint(comment.commentGroupId).empty()
    }

    private fun RemoteCommentGroupWrapper?.empty() {
        Log.d(tag, "cp: $this")
        assertEquals(null, this)
    }

    private fun RemoteComment?.assertEquals(input: RemoteComment) {
        Log.d(tag, "cp: $this")
        assertNotEquals(null, this)
        assertEquals(input, this)
    }
}