package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasource.CommentRemoteDatasourceImpl
import com.wheretogo.data.model.comment.RemoteComment
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
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }

    @Test
    fun commentTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CommentRemoteDatasourceImpl(firestore)
        val cm1 = RemoteComment(
            commentId = "cm1"
        )
        val cm2 = RemoteComment(
            commentId = "cm2"
        )

        assertEquals(true, datasource.addCommentInCheckPoint("cp1", cm1))
        assertEquals(true, datasource.addCommentInCheckPoint("cp1", cm2))

        val cmGroup1 = datasource.getCommentGroupInCheckPoint("cp1")

        assertEquals(true, cmGroup1.isNotEmpty())
        assertEquals(2, cmGroup1.size)
        assertEquals(listOf(cm1, cm2), cmGroup1)
    }

}