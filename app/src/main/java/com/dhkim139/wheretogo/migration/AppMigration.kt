package com.dhkim139.wheretogo.migration

import com.wheretogo.domain.repository.AppRepository
import com.wheretogo.domain.usecase.util.ClearCacheUseCase
import timber.log.Timber
import javax.inject.Inject

class AppMigration @Inject constructor (
    clearCacheUseCase: ClearCacheUseCase,
    private val appRepository: AppRepository
) {
    private val migrations = listOf(
        Migration_1_1(clearCacheUseCase)
    )

    suspend fun run(currentVersion: String) {
        val lastVersion = appRepository.getAppVersion().getOrNull()

        if (lastVersion == null && currentVersion != Migration.FIRST_VERSION) {
            appRepository.setAppVersion(currentVersion)
            return
        }

        migrations
            .filter { isApplicable(it, lastVersion ?: "0.0", currentVersion) }
            .sorted()
            .forEach { migration ->
                runCatching { migration.migrate() }
                    .onSuccess { Timber.d("Migration v${migration.version} 완료") }
                    .onFailure { Timber.e("Migration v${migration.version} 실패: ${it.message}") }
            }

        appRepository.setAppVersion(currentVersion)
    }

    private fun isApplicable(migration: Migration, lastVersion: String, currentVersion: String): Boolean {
        val last = Migration.versionOf(lastVersion)
        val current = Migration.versionOf(currentVersion)
        println("tst_ ${last.version}, ${current.version}")
        return migration > last && migration <= current
    }
}