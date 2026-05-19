package com.wheretogo.data.repositoryimpl

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.domain.repository.MapContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


enum class DefaultMapId {
    SELECT_COURSE_ID, SELECT_CHECKPOINT_ID
}

class MapContentRepositoryImpl @Inject constructor() : MapContentRepository {
    private val _courseList = MutableStateFlow(emptyList<Course>())
    private val _checkpointList = MutableStateFlow(emptyList<CheckPoint>())
    private val _selectedCourseState = MutableStateFlow<CourseDirectionItem?>(null)
    private val _selectedCheckPointState = MutableStateFlow<CheckPoint?>(null)

    override val courseList: StateFlow<List<Course>> = _courseList
    override val checkPointList: StateFlow<List<CheckPoint>> = _checkpointList
    override val selectedCourseState: StateFlow<CourseDirectionItem?> = _selectedCourseState
    override val selectedCheckPointState: StateFlow<CheckPoint?> = _selectedCheckPointState

    override fun refreshCourseList(courses: List<Course>) {
        _courseList.value = courses
    }

    override fun clearCourseList() {
        _courseList.value = emptyList()
    }

    override fun refreshCheckPointList(checkPoints: List<CheckPoint>) {
        _checkpointList.value = checkPoints
    }

    override fun clearCheckPointList() {
        _checkpointList.value = emptyList()
    }

    override fun selectCourse(item: CourseDirectionItem) {
        _selectedCourseState.value = item
    }

    override fun selectCheckPoint(checkPoint: CheckPoint) {
        _selectedCheckPointState.value = checkPoint
    }

    override fun clearCourse() {
        _selectedCourseState.value = null
    }

    override fun clearCheckPoint() {
        _selectedCheckPointState.value = null
    }

    override fun clear() {
        clearCourse()
        clearCheckPoint()
        clearCheckPointList()
        clearCourseList()
    }

    // 유틸
    override fun getLatLngWhenSelected(targetId: String?): LatLng? {
        return targetId?.let { id ->
            when (id) {
                DefaultMapId.SELECT_COURSE_ID.name -> {
                    selectedCourseState.value?.course?.cameraLatLng
                }

                DefaultMapId.SELECT_CHECKPOINT_ID.name -> {
                    selectedCheckPointState.value?.latLng
                }

                else -> null
            }
        }
    }


    override fun getIdWhenSelected(id: String): String? {
        return when (id) {
            DefaultMapId.SELECT_COURSE_ID.name -> {
                selectedCourseState.value?.course?.courseId ?: id
            }

            DefaultMapId.SELECT_CHECKPOINT_ID.name -> {
                selectedCheckPointState.value?.checkPointId ?: id
            }

            else -> null
        }
    }
}
