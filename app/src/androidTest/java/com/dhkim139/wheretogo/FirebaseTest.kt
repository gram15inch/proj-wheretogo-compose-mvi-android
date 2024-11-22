package com.dhkim139.wheretogo


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wheretogo.domain.model.map.Course
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseTest {

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
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }


    @Test
    fun measureFirestoreBatchInsertTime()= runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        
        repeat(500){
            val docRef= db.collection("users").document()
            batch.set(docRef,Course().copy(code=it))
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


    @Test
    fun measureFireStorageInsertTime()= runBlocking{
        val startTime = System.nanoTime()
           val context: Context = ApplicationProvider.getApplicationContext()

        val list = listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9)
        for(item in list) {
            cacheScaleImageFromAsset("photo_original", item)
            delay(100)
        }
        for(item in list) {
            cacheScaleImageFromAsset("photo_opt", item)
            delay(100)
        }

        Log.d("tst3","Data fetch time: ${(System.nanoTime() - startTime) / 1_000_000} ms")
        assertEquals("com.dhkim139.wheretogo", context.packageName)
    }

    fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    suspend fun uploadImageFromDrawable() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val drawable: Drawable = context.getDrawable(R.drawable.photo_original)!!
        val bitmap= drawable.toBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val data = outputStream.toByteArray()

        upload(data,"photo_drawable.jpg")
    }

    suspend fun uploadImageFromAsset() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("photo_original.jpg")
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            outputStream.write(buffer, 0, length)
        }
        val data = outputStream.toByteArray()
       upload(data,"photo_asset.jpg")
    }

    suspend fun uploadCompressImageFromAsset(source:String, compress:Int) {
        val context: Context = ApplicationProvider.getApplicationContext()

        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("$source.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compress, outputStream)
        val compressedData = outputStream.toByteArray()
        upload(compressedData, "${source}_to_comp_$compress.jpg")
    }

    suspend fun uploadScaleImageFormAsset(source: String, scale:Double){
        val context: Context = ApplicationProvider.getApplicationContext()

        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("$source.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        val compress  = 50
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compress, outputStream)
        val scaledData = outputStream.toByteArray()
        upload(scaledData, "${source}_to_scale_${(scale*10).toInt()}_comp_$compress.jpg")
    }

    suspend fun cacheScaleImageFromAsset(source: String, scale:Double){
        val context: Context = ApplicationProvider.getApplicationContext()

        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("$source.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        val compress  = 50
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compress, outputStream)
        val scaledData = outputStream.toByteArray()
        saveCache(scaledData,"thumbnails/${source}_to_scale_${(scale*10).toInt()}_comp_$compress.jpg")
    }

    suspend fun upload(data: ByteArray,fileName:String){
        val storageRef = FirebaseStorage.getInstance().reference.child("FromAndroid/$fileName")
        suspendCancellableCoroutine{ continuation ->
            storageRef.putBytes(data)
                .addOnSuccessListener {
                    Log.d("tst3", "Image uploaded")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { e ->
                    Log.e("tst3", "Upload failed", e)
                    continuation.resumeWithException(e)
                }
        }
    }

    fun saveCache(data: ByteArray, fileName: String){
        val context: Context = ApplicationProvider.getApplicationContext()
        val localFile = File(context.cacheDir, fileName)

        FileOutputStream(localFile).use { outputStream ->
            outputStream.write(data)
        }

    }

}