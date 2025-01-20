package com.dhkim139.wheretogo.remote

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.toRemoteComment
import com.wheretogo.domain.model.dummy.getCommentDummy
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CommentTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun initializeFirebase() {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }
        }
    }

    @Test
    fun setCommentTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)
        val comment = RemoteComment(
            commentId = "cm11",
            commentGroupId = "cp10",
            oneLineReview = "hi"
        )
        datasource.setCommentInCheckPoint(comment, true)
    }

    @Test
    fun removeCommentTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)
        val comment = RemoteComment(
            commentId = "cm11",
            commentGroupId = "cp10",
            oneLineReview = "hi"
        )
        datasource.removeCommentInCheckPoint(comment)
    }

    @Test
    fun setCommentGroupTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)

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
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)

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