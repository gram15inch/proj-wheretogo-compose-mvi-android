package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.wheretogo.data.model.report.LocalReport


@Database(
    entities = [LocalReport::class],
    version = 1,
    exportSchema = false
)
abstract class ReportDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}

@Dao
interface ReportDao {

    @Query("SELECT * FROM LocalReport WHERE reportId = :reportId")
    suspend fun select(reportId: String): LocalReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalReport)

    @Query("DELETE FROM LocalReport WHERE reportId = :reportId")
    suspend fun delete(reportId: String)

}
