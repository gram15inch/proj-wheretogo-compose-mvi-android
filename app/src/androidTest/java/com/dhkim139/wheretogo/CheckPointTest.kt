package com.dhkim139.wheretogo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.ImageLocalDatasourceImpl
import com.wheretogo.data.di.DaoDatabaseModule
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.dummy.cs1
import com.wheretogo.data.model.dummy.cs2
import com.wheretogo.data.model.dummy.cs6
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.repositoryimpl.CheckPointRepositoryImpl
import com.wheretogo.domain.model.map.MetaCheckPoint
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CheckPointTest {
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
    fun checkPointTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CheckPointRemoteDatasourceImpl(firestore)

        val cp1 = RemoteCheckPoint(
            checkPointId = "cp1",
            latLng = DataLatLng(123.321, 123.456),
            titleComment = "cp1 comment",
            imgUrl = "https://testImg12312312312.com/test"
        )

        assertEquals(true, datasource.setCheckPoint(cp1))

        val cp2 = datasource.getCheckPoint(cp1.checkPointId)
        assertEquals(cp2, cp1)
    }

    @Test
    fun getCheckPointTest(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CheckPointRemoteDatasourceImpl(firestore)

        val cp1 = datasource.getCheckPoint("cp1")
        Log.d("tst6", "$cp1")
        assertEquals(true, cp1?.imgUrl?.isNotEmpty())
        assertEquals(true, cp1?.titleComment?.isNotEmpty())
        assertNotEquals(0.0, cp1?.latLng?.latitude)
    }

    @Test
    fun getCheckPointWithMetaTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore()
        val firebaseStorage = FirebaseModule.provideFirebaseStorage()
        val checkPointDao =
            DaoDatabaseModule.run { provideCheckPointDao(provideCheckPointDatabase(appContext)) }
        val checkPointRepository = CheckPointRepositoryImpl(
            checkPointRemoteDatasource = CheckPointRemoteDatasourceImpl(firestore),
            checkPointLocalDatasource = CheckPointLocalDatasourceImpl(checkPointDao),
            imageLocalDatasource = ImageLocalDatasourceImpl(firebaseStorage, appContext)
        )

        val cp1 = checkPointRepository.getCheckPointGroup(MetaCheckPoint(listOf("cp1"), 0)).first()
        Log.d("tst6", "$cp1")
        assertEquals(true, cp1.remoteImgUrl.isNotEmpty())
        assertEquals(true, cp1.titleComment.isNotEmpty())
        assertNotEquals(0.0, cp1.latLng.latitude)

    }

    @Test
    fun initCheckPoint(): Unit = runBlocking {
        val firestore = FirebaseModule.provideFirestore()
        val datasource = CheckPointRemoteDatasourceImpl(firestore)

        val cs1 = cs1.map {
            it.copy(
                titleComment = "\uD83D\uDE0A 주위가 조용해요.",
                imgUrl = "photo_original.jpg"
            )
        }
        val cs2 = cs2.map {
            it.copy(
                titleComment = "\uD83D\uDE0C 경치가 좋아요.",
                imgUrl = "photo_original.jpg"
            )
        }
        val cs6 = cs6.map {
            it.copy(
                titleComment = "\uD83D\uDE1A 또 가고싶어요.",
                imgUrl = "photo_original.jpg"
            )
        }
        // file:///data/user/0/com.dhkim139.wheretogo/cache/down_photo_original_768x1024_70.jpg
        cs1.forEach {
            assertEquals(true, datasource.setCheckPoint(it))
        }
        cs2.forEach {
            assertEquals(true, datasource.setCheckPoint(it))
        }
        cs6.forEach {
            assertEquals(true, datasource.setCheckPoint(it))
        }
    }


    fun Context.cacheScaleImageFromAsset(source: String, w: Int, h: Int, compress: Int): File {
        val path = "$source.jpg"
        val assetManager = this.assets

        val bitmap = assetManager.open(path).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        val rotation = assetManager.open(path).use { inputStream ->
            ExifInterface(inputStream).run {
                when (getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        }
        val rotatedBitmap = if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }


        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, w, h, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compress, outputStream)
        val scaledData = outputStream.toByteArray()
        return saveCache(scaledData, "${source}_${w}x${h}_${compress}.jpg")
    }


    fun Context.saveCache(data: ByteArray, fileName: String): File {
        val cacheSubDir = File(this.cacheDir, "thumbnails")
        if (!cacheSubDir.exists()) {
            cacheSubDir.mkdir()
        }

        val localFile = File(cacheSubDir, fileName)

        FileOutputStream(localFile).use { outputStream ->
            outputStream.write(data)
        }
        return localFile
    }

    @Test
    fun uploadTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val file = appContext.cacheScaleImageFromAsset("photo_original", 75, 100, 80)
        Log.d("tst5", "${file.absoluteFile}")
        assertEquals(true, file.exists())
    }

    @Test
    fun downloadTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val datasource =
            ImageLocalDatasourceImpl(FirebaseModule.provideFirebaseStorage(), appContext)
        val remotePath = "checkpoint/photo_original_768x1024_70.jpg"

        val path = datasource.getImage(remotePath, "small")
        assertEquals("cache_photo_original_768x1024_70.jpg", path.split("/").last())
    }

}