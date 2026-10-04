package com.wheretogo.data.model.route

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.wheretogo.data.model.course.CourseEntity


@Entity(
    tableName = "route_paths",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["routeId"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoutePathEntity(
    @PrimaryKey val routeId: String,
    val format: String,
    val forward: String,
    val backward: String,
    val bytes: Int,
    val lastUsedAt: Long,
)