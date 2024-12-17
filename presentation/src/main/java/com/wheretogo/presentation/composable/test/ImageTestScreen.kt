package com.wheretogo.presentation.composable.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.DecimalFormat

@Composable
fun ImageTestScreen() {
    var imgs by remember { mutableStateOf(emptyList<File>()) }
    val context = LocalContext.current

    LaunchedEffect(rememberCoroutineScope()) {
        // clearGlideCache(context)
        val cacheSubDir = File(context.cacheDir, "thumbnails")
        imgs = getCacheImgs(cacheSubDir)
    }

    FlexibleGrid(imgs, 2)

}

fun getCacheImgs(root: File): List<File> {
    if (!root.exists() || !root.isDirectory) {
        return emptyList()
    }
    return root.listFiles()?.toList() ?: emptyList()
}


@Composable
fun FlexibleGrid(images: List<File>, columnCount: Int) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images.size) { index ->
            val file = images[index]
            val size = 100.dp
            //Log.d("tst3","${file.name} / ${file.path}")
            Column {
                Text(modifier = Modifier.width(size), text = "${file.name}")
                Text(
                    modifier = Modifier.width(size),
                    text = "${getImageResolutionFromFile(file).run { "${this!!.first} x ${this.second}" }}"
                )
                Text(modifier = Modifier.width(size), text = "${getFileSizeInReadableFormat(file)}")
                GlideImage(
                    modifier = Modifier.size(size),
                    imageModel = { file.path },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    ),
                    requestBuilder = {
                        Glide.with(LocalContext.current)
                            .load(file.path)
                            .apply(RequestOptions.bitmapTransform(RotateIfLandscapeTransformation()))
                    }
                )
            }

        }
    }

}

fun getFileSizeInReadableFormat(file: File): String {
    val fileSizeInBytes = file.length()
    val df = DecimalFormat("#.##")

    return when {
        fileSizeInBytes >= 1024 * 1024 -> {
            "${df.format(fileSizeInBytes / (1024.0 * 1024.0))} MB"
        }

        fileSizeInBytes >= 1024 -> {
            "${df.format(fileSizeInBytes / 1024.0)} KB"
        }

        else -> {
            "$fileSizeInBytes Bytes"
        }
    }
}

class RotateIfLandscapeTransformation : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return if (toTransform.width > toTransform.height) {
            val matrix = Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(
                toTransform,
                0,
                0,
                toTransform.width,
                toTransform.height,
                matrix,
                true
            )
        } else {
            toTransform
        }
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("rotate_if_landscape".toByteArray())
    }

    override fun equals(other: Any?) = other is RotateIfLandscapeTransformation

}

fun getImageResolutionFromFile(file: File): Pair<Int, Int>? {
    return if (file.exists()) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        Pair(options.outWidth, options.outHeight)
    } else {
        null
    }
}

suspend fun clearGlideCache(context: Context) {
    withContext(Dispatchers.IO) {
        Glide.get(context).clearDiskCache()
    }
    Glide.get(context).clearMemory()
}