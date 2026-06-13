package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.wheretogo.domain.usecase.util.GetImagesPageUseCase
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.feature.MediaAccess
import com.wheretogo.presentation.model.PickerImage
import com.wheretogo.presentation.model.PickerImage.Companion.toPickerImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaPickerUiState(
    val access: MediaAccess? = null,
    val selected: Set<Long> = emptySet(),
    val confirmed: Boolean = false,
)

enum class MediaPickerUiEvent{
    RefreshPage, RefreshAccess
}

@HiltViewModel
class MediaPickerViewModel  @Inject constructor(
    private val getImagesPage: GetImagesPageUseCase
): ViewModel()  {
    private val _uiState = MutableStateFlow(MediaPickerUiState())
    private val _uiEvent = MutableSharedFlow<MediaPickerUiEvent>()
    val uiState: StateFlow<MediaPickerUiState> = _uiState.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()

    val images: Flow<PagingData<PickerImage>> =
        Pager(
            config = PagingConfig(
                pageSize = 60,
                prefetchDistance = 30,   // 끝 30칸 전부터 미리 로드
                maxSize = 300,           // 멀리 벗어난 페이지는 회수
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { MediaPagingSource(getImagesPage) },
        ).flow.cachedIn(viewModelScope)

    fun toggle(id: Long) {
        _uiState.update { state ->
            val next = if (id in state.selected) state.selected - id
            else state.selected + id
            state.copy(selected = next)
        }
    }

    fun handleError(error: Throwable) {

    }

    fun setAccess(access: MediaAccess?) {
        _uiState.update { it.copy(access = access) }
        if(access != null)
            viewModelScope.launch {
                _uiEvent.emit(MediaPickerUiEvent.RefreshPage)
            }
    }

    fun lifecycleChange(event: AppLifecycle) {
        when (event) {
            AppLifecycle.onResume -> {
                viewModelScope.launch {
                    _uiEvent.emit(MediaPickerUiEvent.RefreshAccess)
                }
            }

            else -> {}
        }
    }

    fun refreshSelection(validIds: Set<Long>) {
        _uiState.update { it.copy(selected = it.selected intersect validIds) }
    }
}


class MediaPagingSource(
    private val getImagesPage: GetImagesPageUseCase,
) : PagingSource<Int, PickerImage>() {

    override fun getRefreshKey(state: PagingState<Int, PickerImage>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PickerImage> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val images = getImagesPage(offset, limit)
                .getOrThrow().map { it.toPickerImage() }
            LoadResult.Page(
                data = images,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = if (images.size < limit) null else offset + limit,
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
