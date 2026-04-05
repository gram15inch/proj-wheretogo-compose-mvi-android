package com.dhkim139.admin.wheretogo.feature.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim139.admin.wheretogo.feature.report.data.ReportRepository
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportDto
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportListUiState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val hasNext: Boolean = false,
    val selectedStatus: ReportStatus? = ReportStatus.PENDING,
    val selectedModerate: ModerateSeverity? = null,
    val errorMessage: String? = null,
    val currentPage: Int = 0,
)

@HiltViewModel
class ReportListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository,
) : ViewModel() {
    private val initStatus: ReportStatus = savedStateHandle
        .get<String>("reportStatus")
        ?.takeIf { it.isNotBlank() }
        ?.let { ReportStatus.from(it) }
        ?: ReportStatus.PENDING

    private val initModerate: ModerateSeverity? = savedStateHandle
        .get<String>("reportModerate")
        ?.takeIf { it.isNotBlank() }
        ?.let { ModerateSeverity.from(it) }

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initUiState()
        loadReports(reset = true)
    }

    private fun initUiState(){
        _uiState.update { it.copy(selectedStatus = initStatus, selectedModerate = initModerate) }
    }

    fun selectStatus(status: ReportStatus?) {
        if (_uiState.value.selectedStatus == status) return
        _uiState.update { it.copy(selectedStatus = status) }
        loadReports(reset = true)
    }

    fun selectModerate(moderate: ModerateSeverity?) {
        if (_uiState.value.selectedModerate == moderate) return
        _uiState.update { it.copy(selectedModerate = moderate) }
        loadReports(reset = true)
    }

    fun loadNextPage() {
        if (!_uiState.value.hasNext || _uiState.value.isLoading) return
        loadReports(reset = false)
    }

    fun refresh() = loadReports(reset = true)

    private fun loadReports(reset: Boolean) {
        val currentState = _uiState.value
        val nextPage = if (reset) 0 else currentState.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getReports(
                status = currentState.selectedStatus?.raw,
                moderate = currentState.selectedModerate?.raw,
                page = nextPage,
            ).onSuccess { response ->
                val newReports = response.reports.map(ReportDto::toDomain)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reports = if (reset) newReports else it.reports + newReports,
                        hasNext = response.hasNext,
                        currentPage = nextPage,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
