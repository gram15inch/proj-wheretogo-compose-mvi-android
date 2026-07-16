package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.usecase.gallery.LoadGalleryPhotosUseCase
import com.wheretogo.presentation.model.MiniPhoto
import com.wheretogo.presentation.toMiniPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val loadGalleryPhotosUseCase: LoadGalleryPhotosUseCase,
) : ViewModel() {
    private var _cachedPhotos: List<GalleryPhoto> = emptyList()

    val stamps: StateFlow<List<MiniPhoto>> = loadGalleryPhotosUseCase.observe()
        .map { it
            .filter { it.stampAt != null }
            .sortedByDescending { it.stampAt }
            .take(4)
            .map { it.toMiniPhoto() }
        }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    init {
        observeGalleryPhotos()
    }

    private fun observeGalleryPhotos() {
        loadGalleryPhotosUseCase.observe()
            .onStart { delay(310) } // 화면 이동 대기
            .onEach { photos ->
                _cachedPhotos = photos
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
}