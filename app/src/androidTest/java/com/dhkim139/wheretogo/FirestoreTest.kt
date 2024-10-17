package com.dhkim139.wheretogo


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.domain.model.Course
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreTest {


    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }


    @Test
    fun measureFirestoreBatchInsertTime()= runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        
        repeat(500){
            val docRef= db.collection("users").document()
            batch.set(docRef,Course.empty().copy(code=it))
        }
        val startTime = System.nanoTime()

        suspendCancellableCoroutine{ continuation ->

             batch.commit()
                 .addOnSuccessListener { documentReference ->
                     val endTime = System.nanoTime()
                     val duration = (endTime - startTime) / 1_000_000
                     Log.d("tst2","Data fetch time: $duration ms")
                     continuation.resume(Unit)
                 }
                 .addOnFailureListener { e ->
                     continuation.resumeWithException(e)
                 }


         }

        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }
}