package com.dhkim139.wheretogo.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun createDummy(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        context.apply {
            runBlocking {
                cacheScaleImageFromAsset("photo_original",75,100,50)
                cacheScaleImageFromAsset("photo_original",75,100,60)
                cacheScaleImageFromAsset("photo_original",75,100,70)
                cacheScaleImageFromAsset("photo_original",75,100,80)
                cacheScaleImageFromAsset("photo_original",150,200,70)
                cacheScaleImageFromAsset("photo_original",150,200,80)
                cacheScaleImageFromAsset("photo_original",768,1024,70)
                cacheScaleImageFromAsset("photo_original",768,1024,80)
                cacheScaleImageFromAsset("photo_original",960,1280,70)
                cacheScaleImageFromAsset("photo_original",960,1280,80)
            }
        }
    }
}

fun Context.cacheScaleImageFromAsset(source: String, w: Int, h: Int, compress: Int) {
    val path = "$source.jpg"
    val assetManager = this.assets

    val bitmap = assetManager.open(path).use { inputStream->
        BitmapFactory.decodeStream(inputStream)
    }
    val rotation = assetManager.open(path).use { inputStream->
        ExifInterface(inputStream).run {
            when(getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)){
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
    saveCache(scaledData, "${source}_${w}x${h}_${compress}.jpg")
}


fun Context.saveCache(data: ByteArray, fileName: String){
    val cacheSubDir = File(this.cacheDir, "thumbnails")
    if (!cacheSubDir.exists()) {
        cacheSubDir.mkdir()  // 폴더가 없으면 생성
    }

    val localFile = File(cacheSubDir, fileName)

    FileOutputStream(localFile).use { outputStream ->
        outputStream.write(data)
    }

}