package com.dhkim139.wheretogo.migration

import com.wheretogo.domain.model.util.AppCache
import com.wheretogo.domain.usecase.util.ClearCacheUseCase


interface Migration : Comparable<Migration> {
    val version: String

    suspend fun migrate()

    override fun compareTo(other: Migration): Int {
        val segmentsA = version.toNumSegments()
        val segmentsB = other.version.toNumSegments()
        val maxLen = maxOf(segmentsA.size, segmentsB.size)

        for (i in 0 until maxLen) {
            val a = segmentsA.getOrNull(i)?:0
            val b = segmentsB.getOrNull(i)?:0
            when {
                a > b -> return 1
                a < b -> return -1
            }
        }

        return 0
    }

    private fun String.toNumSegments() =
        substringBefore("-")
            .split(".")
            .map { it.toIntOrNull() ?: 0 }

    companion object {
        fun versionOf(version: String) = object : Migration {
            override val version = version
            override suspend fun migrate() = Unit
        }
        const val FIRST_VERSION = "1.1"
    }
}


class Migration_1_1(
    private val clearCacheUseCase: ClearCacheUseCase
) : Migration {
    override val version = "1.1"
    override suspend fun migrate() {
        clearCacheUseCase(setOf(AppCache.USER, AppCache.CONTENT, AppCache.HISTORY))
    }
}

