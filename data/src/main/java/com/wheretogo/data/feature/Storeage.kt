package com.wheretogo.data.feature

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.map.Mapper
import coil.request.Options
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.wheretogo.data.model.confg.ImageConfig
import kotlinx.coroutines.tasks.await


class StoragePathMapper : Mapper<String, StorageReference> {
    override fun map(data: String, options: Options): StorageReference? {
        if (!data.startsWith("/image/")) return null
        return Firebase.storage.reference.child(data)
    }
}

class StorageReferenceFetcher(
    private val data: StorageReference,
    private val imgConfig: ImageConfig,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): SourceResult {
        val bytes =
            data.getBytes(imgConfig.maxDownMB.toLong() * 1024 * 1024).await()

        return SourceResult(
            source = ImageSource(
                source = okio.Buffer().apply { write(bytes) },
                context = options.context,
            ),
            mimeType = null,
            dataSource = DataSource.NETWORK,
        )
    }

    class Factory(val imgConfig: ImageConfig) : Fetcher.Factory<StorageReference> {
        override fun create(
            data: StorageReference,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = StorageReferenceFetcher(data, imgConfig,options)
    }
}