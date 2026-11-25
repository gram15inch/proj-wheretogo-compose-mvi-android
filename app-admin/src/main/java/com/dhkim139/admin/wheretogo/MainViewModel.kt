package com.dhkim139.admin.wheretogo

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.usecase.app.AppCheckBySignatureUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isReady: Boolean = false,
    val isLoggedIn: Boolean = false,
    val mail: String? = null,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCheckUseCase: AppCheckBySignatureUseCase,
    private val getUserProfileStreamUseCase: GetUserProfileStreamUseCase,
    private val userSignOutUseCase: UserSignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            appCheckUseCase()
                .onSuccess { showToast ->
                    if (showToast) {
                        Toast.makeText(context, "앱 검증 완료", Toast.LENGTH_SHORT).show()
                    }
                }
                .onFailure {
                    Toast.makeText(context, "앱 검증 실패", Toast.LENGTH_SHORT).show()
                }
            val profile =  getUserProfileStreamUseCase().first().getOrNull()
            val isLoggedIn = profile?.uid?.isNotBlank() ?: false
            val mail = profile?.private?.mail
            _uiState.update {
                it.copy(isReady = true, isLoggedIn = isLoggedIn, mail = mail)
            }
        }
    }

    fun signOut(){
        viewModelScope.launch(Dispatchers.IO){
            userSignOutUseCase()
        }
    }
}