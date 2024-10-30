package com.dhkim139.wheretogo.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun createDummy(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        context.apply {
            cacheScaleImageFromAsset("photo_original",100,75,50)
            cacheScaleImageFromAsset("photo_original",100,75,60)
            cacheScaleImageFromAsset("photo_original",100,75,70)
            cacheScaleImageFromAsset("photo_original",100,75,80)
            cacheScaleImageFromAsset("photo_original",200,150,70)
            cacheScaleImageFromAsset("photo_original",200,150,80)
            cacheScaleImageFromAsset("photo_original",1024,768,70)
            cacheScaleImageFromAsset("photo_original",1024,768,80)
            cacheScaleImageFromAsset("photo_original",1280,960,70)
            cacheScaleImageFromAsset("photo_original",1280,960,80)
        }
    }
}

fun Context.cacheScaleImageFromAsset(source: String, scale:Double){
    val assetManager = this.assets
    val inputStream: InputStream = assetManager.open("$source.jpg")
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val width = (bitmap.width * scale).toInt()
    val height = (bitmap.height * scale).toInt()
    val compress  = 80
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compress, outputStream)
    val scaledData = outputStream.toByteArray()
    saveCache(scaledData,"${source}_to_scale_1_2_comp_$compress.jpg")
}

fun Context.cacheScaleImageFromAsset(source: String, w: Int, h: Int, compress: Int) {
    val assetManager = this.assets
    val inputStream: InputStream = assetManager.open("$source.jpg")
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
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