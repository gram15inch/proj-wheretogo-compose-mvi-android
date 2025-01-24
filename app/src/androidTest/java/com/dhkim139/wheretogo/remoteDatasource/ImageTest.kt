package com.dhkim139.wheretogo.remoteDatasource

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasourceimpl.ImageRemoteDatasourceImpl
import com.wheretogo.data.repositoryimpl.ImageRepositoryImpl
import com.wheretogo.domain.ImageSize
import com.wheretogo.presentation.feature.getAssetFileUri
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import javax.inject.Inject

@HiltAndroidTest
class ImageTest {
    private val tag = "tst_image"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var remote: ImageRemoteDatasourceImpl

    @Inject
    lateinit var local: ImageLocalDatasource


    @Before
    fun init() {
        hiltRule.inject()
    }

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
    fun setAndGetImageTest(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = getAssetFileUri(context, "photo_opt.jpg")!!
        val repository = ImageRepositoryImpl(remote, local)
        val imageName = "testImage.jpg"

        assertEquals(true, repository.setImage(uri, imageName))
        assertEquals(true, repository.getImage(imageName, ImageSize.NORMAL)!!.exists())
        assertEquals(true, repository.getImage(imageName, ImageSize.SMALL)!!.exists())

        assertEquals(true, repository.removeImage(imageName))
        assertEquals(null, repository.getImage(imageName, ImageSize.NORMAL))
        assertEquals(null, repository.getImage(imageName, ImageSize.SMALL))
    }
}