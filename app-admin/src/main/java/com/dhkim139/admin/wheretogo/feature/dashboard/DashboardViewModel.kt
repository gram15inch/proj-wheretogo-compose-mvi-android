package com.dhkim139.admin.wheretogo.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportDto
import com.dhkim139.admin.wheretogo.feature.report.data.ReportRepository
import com.wheretogo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val stats: DashboardStatsDto? = null,
    val recentReports: List<Report> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 프로필
            launch {
                val user = userRepository.getProfileStream().first()
                _uiState.update { it.copy(email = user.private.mail) }
            }

            // 대시보드
            val dashboard = async {
                val statsResult = reportRepository.getDashboardStats()
                _uiState.update { state ->
                    state.copy(
                        stats = statsResult.getOrNull(),
                        errorMessage = statsResult.exceptionOrNull()?.message,
                    )
                }
                statsResult
            }

            // 최근 신고 목록
            val reports = async {
                val reportsResult = reportRepository.getReports(status = "PENDING", size = 5)
                _uiState.update { state ->
                    state.copy(
                        recentReports = reportsResult.getOrNull()
                            ?.reports
                            ?.map(ReportDto::toDomain)
                            ?: emptyList(),
                        errorMessage = reportsResult.exceptionOrNull()?.message,
                    )
                }
                reportsResult
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearUserCache()
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
