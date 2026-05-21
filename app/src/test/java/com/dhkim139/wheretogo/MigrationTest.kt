package com.dhkim139.wheretogo

import com.dhkim139.wheretogo.migration.Migration
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MigrationTest {

    @Test
    fun `버전이 같을시 0 반환`() {
        val a = Migration.versionOf("1.0.0")
        val b = Migration.versionOf("1.0.0")

        assertThat(0).isEqualTo(a.compareTo(b))
    }

    @Test
    fun `sorted시 버전순 정렬`() {
        val migrations = listOf(
            Migration.versionOf("1.3"),
            Migration.versionOf("1.10.0"),
            Migration.versionOf("1.1.0"),
            Migration.versionOf("1.9.0-rc3"),
        ).sorted()

        assertThat(listOf("1.1.0", "1.3", "1.9.0-rc3", "1.10.0"))
            .isEqualTo(migrations.map { it.version })

    }
}