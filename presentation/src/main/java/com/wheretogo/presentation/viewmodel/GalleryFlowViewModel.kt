package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.handler.GalleryFlowHandler
import com.wheretogo.domain.handler.GalleryFlowMsgEvent
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.usecase.gallery.DeleteGalleryPhotosUseCase
import com.wheretogo.domain.usecase.gallery.LoadGalleryPhotosUseCase
import com.wheretogo.domain.usecase.gallery.SavePickedImagesUseCase
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.feature.ByCourseGrouping
import com.wheretogo.presentation.feature.ByDayGrouping
import com.wheretogo.presentation.feature.GroupingStrategy
import com.wheretogo.presentation.feature.toSections
import com.wheretogo.presentation.intent.GalleryIntent
import com.wheretogo.presentation.model.PickerImage
import com.wheretogo.presentation.state.GalleryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


sealed interface GalleryUiEffect {
    data class NavigateToDetail(val photoId: Long) : GalleryUiEffect
}

@HiltViewModel
class GalleryFlowViewModel @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val handler: GalleryFlowHandler,
    private val savePickedImagesUseCase: SavePickedImagesUseCase,
    private val loadGalleryPhotos: LoadGalleryPhotosUseCase,
    private val deleteGalleryPhotos: DeleteGalleryPhotosUseCase,
) : ViewModel() {
    private val _galleyState =
        MutableStateFlow<GalleryState>(GalleryState.Loading)
    val galleryState: StateFlow<GalleryState> = _galleyState.asStateFlow()

    private val _effect = MutableSharedFlow<GalleryUiEffect>()
    val effect: SharedFlow<GalleryUiEffect> = _effect.asSharedFlow()

    val groupings: List<GroupingStrategy> = listOf(ByCourseGrouping(),ByDayGrouping())
    private var _grouping: GroupingStrategy = groupings.first()
    val currentGroupingLabel: String get() = _grouping.label

    private var _cachedPhotos: List<GalleryPhoto> = emptyList()
    private val _detailPhotoId = MutableStateFlow<Long?>(null)
    val detailPhoto: StateFlow<GalleryPhoto?> = _detailPhotoId
        .map { id -> id?.let { selectedId -> _cachedPhotos.firstOrNull { it.id == selectedId } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        initSuccess()
        viewModelScope.launch(dispatcher) {
            withContext(Dispatchers.IO){
                delay(350)
                handleIntent(GalleryIntent.Refresh)
            }
        }
    }

    fun handleIntent(intent: GalleryIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                is GalleryIntent.Refresh -> refreshGallery()
                is GalleryIntent.MediaPicked -> onMediaPicked(intent.images)

                is GalleryIntent.ChangeGrouping -> changeGrouping(intent.strategy)
                is GalleryIntent.PhotoClick -> onPhotoClick(intent.pickerId)
                is GalleryIntent.PhotoLongClick -> onPhotoLongClick(intent.pickerId)
                is GalleryIntent.ClearSelection -> clearSelection()
                is GalleryIntent.SelectAll -> selectAll()
                is GalleryIntent.DeleteSelected -> deleteSelected()

                is GalleryIntent.OpenDetail -> openDetail(intent.id)
                is GalleryIntent.CloseDetail -> closeDetail()
            }
        }
    }

    suspend fun handleError(e: Throwable, event: GalleryFlowMsgEvent?=null){
        handler.handle(e,event)
    }

    // 갤러리 갱신
    private suspend fun onMediaPicked(images: List<PickerImage>) {
        if (images.isEmpty()) return
        _galleyState.value = GalleryState.Loading
        val mediaImages = images.map { it.toMarkerImage() }
        withContext(Dispatchers.IO) { savePickedImagesUseCase(mediaImages) }
            .onSuccess { refreshGallery() }
            .onFailure { handleError(it, GalleryFlowMsgEvent.MEDIA_SAVE_FAIL) }

    }

    private suspend fun refreshGallery() {
        withContext(Dispatchers.IO) { loadGalleryPhotos() }
            .onSuccess { photos ->
                _cachedPhotos = photos
                initSuccess()
            }.onFailure {
                _galleyState.value = GalleryState.Error("")
                handleError(it, GalleryFlowMsgEvent.GALLERY_LOAD_FAIL)
            }
    }


    // 갤러리 로드 성공시
    private fun changeGrouping(strategy: GroupingStrategy) {
        if (strategy.label == _grouping.label) return
        _galleyState.value.onSuccess {
            _grouping = strategy
            initSuccess(selectedIds =  it.selectedIds)
        }
    }

    private suspend fun onPhotoClick(pickerId: Long) {
        _galleyState.value.onSuccessAwait {
            if (it.isSelectionMode) {
                it.toggleSelection(pickerId)
            } else {
                _effect.emit(GalleryUiEffect.NavigateToDetail(pickerId))
            }
        }
    }

    private fun onPhotoLongClick(pickerId: Long) {
        _galleyState.value.onSuccess {
            it.toggleSelection(pickerId)
        }
    }

    private fun clearSelection() {
        _galleyState.value.onSuccess {
            _galleyState.value = it.copy(selectedIds = emptySet())
        }
    }

    private fun selectAll() {
        _galleyState.value.onSuccess {
            _galleyState.value = it.copy(selectedIds = it.allPhotos.map { it.id }.toSet())
        }
    }

    private suspend fun deleteSelected() {
        _galleyState.value.onSuccessAwait {
            val targets = it.selectedIds
            if (targets.isEmpty()) return@onSuccessAwait
            withContext(Dispatchers.IO) { deleteGalleryPhotos(targets) }
                .onSuccess { deletedIds ->
                    _cachedPhotos = _cachedPhotos.filterNot { it.id in deletedIds }
                    initSuccess()
                }.onFailure {
                    handleError(it, GalleryFlowMsgEvent.PHOTO_DELETE_FAIL)
                }
        }
    }

    // 상세화면
    private suspend fun openDetail(id: Long) {
        _detailPhotoId.emit(id)
    }

    private suspend fun closeDetail() {
        _detailPhotoId.emit(null)
    }



    // 헬퍼
    private fun initSuccess(selectedIds: Set<Long> = emptySet()) {
        val sections = _cachedPhotos.toSections(_grouping)
        val validSelected = selectedIds intersect _cachedPhotos.map { it.id }.toSet()
        _galleyState.value = GalleryState.Success(
            sections = sections,
            selectedIds = validSelected,
            groupingLabel = _grouping.label,
        )
    }

    private fun GalleryState.Success.toggleSelection(id: Long) {
        val newSelected = selectedIds.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        _galleyState.value = copy(selectedIds = newSelected)
    }
}