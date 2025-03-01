package com.dhkim139.wheretogo.remoteDatasource

import androidx.test.platform.app.InstrumentationRegistry
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
import javax.inject.Inject

@HiltAndroidTest
class ImageTest {
    private val tag = "tst_image"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var imageRemoteDatasourceImpl: ImageRemoteDatasourceImpl

    @Inject
    lateinit var imageLocalDatasourceImpl: ImageLocalDatasource


    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun setAndGetImageTest(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = getAssetFileUri(context, "photo_opt.jpg")!!
        val repository = ImageRepositoryImpl(imageRemoteDatasourceImpl, imageLocalDatasourceImpl)

        val imageName = repository.setImage(uri).getOrNull()!!

        assertEquals(true, repository.getImage(imageName, ImageSize.NORMAL).getOrNull()!!.exists())
        assertEquals(true, repository.getImage(imageName, ImageSize.SMALL).getOrNull()!!.exists())

        assertEquals(true, repository.removeImage(imageName).isSuccess)
        assertEquals(null, repository.getImage(imageName, ImageSize.NORMAL).getOrNull())
        assertEquals(null, repository.getImage(imageName, ImageSize.SMALL).getOrNull())
    }
}