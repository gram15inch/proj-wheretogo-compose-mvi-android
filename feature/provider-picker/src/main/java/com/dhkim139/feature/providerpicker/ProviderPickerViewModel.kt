package com.dhkim139.feature.providerpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.dhkim139.feature.providerpicker.model.ProviderPickerItem
import com.dhkim139.feature.providerpicker.model.ProviderPickerItem.Companion.toPickerImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProviderPickerUiState(
    val selected: Set<Long> = emptySet(),
    val confirmed: Boolean = false,
)

enum class ProviderPickerUiEvent{
    RefreshPage
}

@HiltViewModel
class ProviderPickerViewModel  @Inject constructor(
    private val getImagesPage: GetImagesPageUseCase
): ViewModel()  {
    private val _uiState = MutableStateFlow(ProviderPickerUiState())
    private val _uiEvent = MutableSharedFlow<ProviderPickerUiEvent>()
    val uiState: StateFlow<ProviderPickerUiState> = _uiState.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()

    val images: Flow<PagingData<ProviderPickerItem>> =
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

    fun refreshSelection(validIds: Set<Long>) {
        _uiState.update { it.copy(selected = it.selected intersect validIds) }
    }
}


class MediaPagingSource(
    private val getImagesPage: GetImagesPageUseCase,
) : PagingSource<Int, ProviderPickerItem>() {

    override fun getRefreshKey(state: PagingState<Int, ProviderPickerItem>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProviderPickerItem> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val images =
                getImagesPage(offset, limit)
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