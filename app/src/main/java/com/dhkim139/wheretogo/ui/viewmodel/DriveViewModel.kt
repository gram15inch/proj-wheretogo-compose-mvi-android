package com.dhkim139.wheretogo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.data.model.map.LocalMap
import com.dhkim139.wheretogo.domain.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(private val mapRepository: MapRepository) :ViewModel() {

    suspend fun getMap(course:Course):LocalMap{
        return mapRepository.getMap(course)
    }
}