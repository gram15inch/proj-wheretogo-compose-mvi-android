package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import com.wheretogo.domain.model.user.Profile
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserTest {

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
    fun getAndSetProfileTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val remoteDatasource = UserRemoteDatasourceImpl(firestore)

        val p1 = Profile(
            uid = "uid1",
            name = "name",
            mail = "mail",
            authCompany = "google",
            lastVisited = 0L,
            accountCreation = 0L,
            isAdRemove = false
        )

        remoteDatasource.setProfile(p1)
        val result = remoteDatasource.getProfile(p1.uid)
        assertEquals(p1, result)
        remoteDatasource.removeProfile(p1.uid)
    }

    @Test
    fun modifyProfileTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val remoteDatasource = UserRemoteDatasourceImpl(firestore)

        val p1 = Profile(
            uid = "uid1",
            name = "name",
            mail = "mail",
            authCompany = "google",
            lastVisited = 0L,
            accountCreation = 0L,
            isAdRemove = false
        )

        val p1Copy = p1.copy(name = "modifyName")

        remoteDatasource.setProfile(p1)

        assertEquals(p1, remoteDatasource.getProfile(p1.uid))


        remoteDatasource.setProfile(p1Copy)

        assertEquals(p1Copy, remoteDatasource.getProfile(p1.uid))

        remoteDatasource.removeProfile(p1.uid)
    }


    @Test
    fun removeProfileTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val remoteDatasource = UserRemoteDatasourceImpl(firestore)

        val p1 = Profile(
            uid = "uid1",
            name = "name",
            mail = "mail",
            authCompany = "google",
            lastVisited = 0L,
            accountCreation = 0L,
            isAdRemove = false
        )

        remoteDatasource.setProfile(p1)

        assertEquals(p1, remoteDatasource.getProfile(p1.uid))

        remoteDatasource.removeProfile(p1.uid)

        assertEquals(null, remoteDatasource.getProfile(p1.uid))

    }


    suspend fun <T : Any> DocumentReference.mySet(data: T): Boolean {
        return suspendCancellableCoroutine { continuation ->
            this.set(data).addOnSuccessListener {
                continuation.resume(true)
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }
    }
}