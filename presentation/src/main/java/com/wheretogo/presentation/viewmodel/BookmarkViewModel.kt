package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.state.BookmarkScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val userRepository: UserRepository,
) :
    ViewModel() {
    private val _bookMarkScreenState = MutableStateFlow(BookmarkScreenState())
    val bookMarkScreenState: StateFlow<BookmarkScreenState> = _bookMarkScreenState


    fun addBookmark(bookmarkId: String) {
        viewModelScope.launch {

        }
    }

    fun removeBookmark(bookmarkId: String) {
        viewModelScope.launch {

        }
    }
}