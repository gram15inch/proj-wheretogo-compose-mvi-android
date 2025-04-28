
-keep class com.wheretogo.data.model.** { *; }
-keep class com.wheretogo.data.datasourceimpl.service.** { *; }
-keep class com.wheretogo.data.datasourceimpl.database.** { *; }

-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}