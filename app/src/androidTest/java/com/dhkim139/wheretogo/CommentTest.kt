package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteCommentGroupWrapper
import com.wheretogo.data.repositoryimpl.CommentRepositoryImpl
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

        val list = (1..12).map {
            RemoteCommentGroupWrapper("cp$it",
                getCommentDummy("cp$it").map {
                    it.toRemoteComment()
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


    @Test
    fun getCommentRepositoryTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)
        val repository = CommentRepositoryImpl(datasource)
        val list = (1..12).map {
            RemoteCommentGroupWrapper("cp$it",
                getCommentDummy("cp$it").map {
                    it.toRemoteComment()
                }
            )
        }

        list.forEach {
            val list = repository.getComment(it.groupId)
            assertEquals(true, list.isNotEmpty())
            assertEquals(it.groupId, list.first().groupId)
            assertEquals(6, list.size)
        }
    }

}