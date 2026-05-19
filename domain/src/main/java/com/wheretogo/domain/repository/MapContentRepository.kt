package com.wheretogo.domain.repository

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseDirectionItem
import kotlinx.coroutines.flow.StateFlow


enum class DefaultMapId {
    SELECT_COURSE_ID, SELECT_CHECKPOINT_ID
}


interface MapContentRepository {

    val courseList: StateFlow<List<Course>>
    val checkPointList: StateFlow<List<CheckPoint>>
    val selectedCourseState: StateFlow<CourseDirectionItem?>
    val selectedCheckPointState: StateFlow<CheckPoint?>

    fun refreshCourseList(courses: List<Course>)
    fun clearCourseList()

    fun refreshCheckPointList(checkPoints: List<CheckPoint>)
    fun clearCheckPointList()


    fun selectCourse(item: CourseDirectionItem)
    fun selectCheckPoint(checkPoint: CheckPoint)

    fun clearCourse()
    fun clearCheckPoint()

    fun clear()

    // 유틸
    fun getLatLngWhenSelected(targetId: String?): LatLng?


    fun getIdWhenSelected(id: String): String?
}
