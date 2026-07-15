package com.wheretogo.presentation.feature

import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.presentation.model.PhotoSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface GroupingStrategy {
    // 그룹 이름
    val label: String

    // 그룹간 순서
    val comparator: Comparator<GalleryPhoto>

    // 그룹 기준
    fun keyOf(photo: GalleryPhoto): String

    // 그룹 표시
    fun titleOf(key: String, sample: GalleryPhoto): String
}

fun List<GalleryPhoto>.toSections(strategy: GroupingStrategy): List<PhotoSection> =
    this.sortedWith(strategy.comparator)
        .groupBy { strategy.keyOf(it) }
        .map { (key, photos) ->
            PhotoSection(
                key = key,
                title = strategy.titleOf(key, photos.first()),
                hasStamp = photos.any { it.stampAt != null },
                photos = photos
            )
        }

class ByDayGrouping : GroupingStrategy {

    override val label: String = "날짜별"

    override val comparator: Comparator<GalleryPhoto> =
        compareByDescending<GalleryPhoto> { it.exif.dateTaken != null }
            .thenByDescending { it.exif.dateTaken ?: Long.MIN_VALUE }

    override fun keyOf(photo: GalleryPhoto): String {
        val ts = photo.exif.dateTaken ?: return KEY_UNKNOWN
        return dayKeyFormat.format(Date(ts))
    }

    override fun titleOf(key: String, sample: GalleryPhoto): String =
        if (key == KEY_UNKNOWN) "날짜 없음"
        else titleFormat.format(Date(sample.exif.dateTaken!!))

    private companion object {
        const val KEY_UNKNOWN = "unknown"
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val titleFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
    }
}

class ByCourseGrouping : GroupingStrategy {

    override val label: String = "코스별"

    override val comparator: Comparator<GalleryPhoto> =
        compareByDescending<GalleryPhoto> { it.courseId != null && it.courseName != null }
            .thenBy {
                when (it.courseName) {
                    null, KEY_UNKNOWN -> 2
                    else -> 0
                }
            }.thenBy { it.courseName ?: KEY_UNKNOWN }
            .thenByDescending { it.exif.dateTaken ?: Long.MIN_VALUE }

    override fun keyOf(photo: GalleryPhoto): String = photo.courseName ?: KEY_UNKNOWN

    override fun titleOf(key: String, sample: GalleryPhoto): String =
        when (key) {
            KEY_UNKNOWN -> "미확인"
            else -> key
        }

    private companion object {
        const val KEY_UNKNOWN = ""
    }
}