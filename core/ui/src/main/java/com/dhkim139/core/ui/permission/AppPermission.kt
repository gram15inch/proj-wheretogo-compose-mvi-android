package com.dhkim139.core.ui.permission

import android.Manifest
import android.os.Build

sealed class AppPermission(val names: List<String>) {
    data object LOCATION : AppPermission(
        buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    )
    data object MEDIA : AppPermission(
        buildList {
            val api = Build.VERSION.SDK_INT
            if (api >= 34) add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            if (api >= 33) add(Manifest.permission.READ_MEDIA_IMAGES)
            if (api >= 29) add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            if (api >= 16 && api < 33) add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    )

    data object UNKNOWN : AppPermission(listOf("UNKNOWN"))

    fun isEqual(other: Set<String>): Boolean {
        return names.toSet().hashCode() == other.toSet().hashCode()
    }

    companion object {
        fun valueOf(names: Set<String>): AppPermission {
            return when {
                LOCATION.isEqual(names) -> LOCATION
                MEDIA.isEqual(names) -> MEDIA
                else -> UNKNOWN
            }
        }
    }
}