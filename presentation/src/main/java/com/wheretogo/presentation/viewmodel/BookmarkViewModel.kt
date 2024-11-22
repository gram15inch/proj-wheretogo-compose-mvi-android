package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.JourneyRepository
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.state.BookmarkScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BookmarkViewModel @Inject constructor(
    val userRepository: UserRepository,
    journeyRepository: JourneyRepository
) :
    ViewModel() {
    private val _bookMarkScreenState = MutableStateFlow(BookmarkScreenState())
    val bookMarkScreenState: StateFlow<BookmarkScreenState> = _bookMarkScreenState

    init {
        viewModelScope.launch {
            userRepository.getBookmarkFlow().collect { bookmarks ->
                val data = bookmarks.mapNotNull { bk ->
                    journeyRepository.getJourney(bk)
                }.map { it.copy(isBookmark = true) }
                _bookMarkScreenState.value = _bookMarkScreenState.value.copy(data = data)
            }

        }

    }

    fun addBookmark(code: Int) {
        viewModelScope.launch {
            userRepository.addBookmark(code)
        }
    }

    fun removeBookmark(code: Int) {
        viewModelScope.launch {
            userRepository.removeBookmark(code)
        }
    }
}