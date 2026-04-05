package com.dhkim139.admin.wheretogo.feature.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim139.admin.wheretogo.feature.report.data.ReportRepository
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportInput
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.content.ContentType
import com.wheretogo.domain.repository.CheckPointRepository
import com.wheretogo.domain.repository.CommentRepository
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportDetailUiState(
    val isLoading: Boolean = false,
    val report: Report? = null,
    val content: Any? = null,
    val isProcessing: Boolean = false,
    val processSuccess: Boolean = false,
    val errorMessage: String? = null,
    val actionState: ActionState? = null,
)

data class ActionState(
    val type: ActionButtonType
)

enum class ActionButtonType(val label: String) {
    BLOCK_WRITER_AND_HIDE_CONTENT("작성자 차단 · 컨텐츠 숨기기"),
    HIDE_CONTENT("컨텐츠 숨기기"),
    BLOCK_REPORTER_AND_REJECT("신고자 차단 · 컨텐츠 보이기"),
    REJECT("컨텐츠 보이기"),
}

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val courseRepository: CourseRepository,
    private val checkpointRepository: CheckPointRepository,
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            reportRepository.getReport(reportId).onSuccess { report ->
                _uiState.update { it.copy(isLoading = false, report = report) }
                loadContent(report.type, report.contentId, report.contentGroupId)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun loadContent(type: String, contentId: String, contentGroupId: String?) {
        viewModelScope.launch {
            when (type) {
                ContentType.COURSE.name -> {
                    courseRepository.getCourse(contentId)
                }

                ContentType.CHECKPOINT.name -> {
                    checkpointRepository.getCheckPoint(contentId, true).mapSuccess { content ->
                        imageRepository.getImage(content.imageId, ImageSize.SMALL).map { imgUrl ->
                            content.copy(thumbnail = imgUrl)
                        }
                    }
                }

                ContentType.COMMENT.name -> {
                    if (contentGroupId.isNullOrEmpty())
                        return@launch
                    commentRepository.getCommentByGroupId(contentGroupId).map { group ->
                        group.firstOrNull { cmt -> cmt.commentId == contentId }
                            ?: Result.failure<Comment>(Exception("unknown id ${contentId}"))
                    }
                }

                else -> {
                    Result.failure<Unit>(Exception("unknown type"))
                }
            }.onSuccess { content ->
                _uiState.update { it.copy(content = content) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun requestAction(type: ActionButtonType) {
        _uiState.update { it.copy(actionState = ActionState(type)) }
    }

    fun cancelAction() {
        _uiState.update { it.copy(actionState = null) }
    }

    fun confirmAction(input: ReportInput) {
        val type = _uiState.value.actionState?.type ?: return
        val reason = input.reason?.trim()
        if (reason?.isBlank() ?: false) { //  입력 필수가 아닐 경우 null로 들어옴
            _uiState.update { it.copy(errorMessage = "처리 사유를 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, actionState = null) }
            val result = when (type) {
                ActionButtonType.BLOCK_WRITER_AND_HIDE_CONTENT ->
                    reportRepository.approveReport(reportId, true, input)

                ActionButtonType.HIDE_CONTENT ->
                    reportRepository.approveReport(reportId, false, input)

                ActionButtonType.BLOCK_REPORTER_AND_REJECT ->
                    reportRepository.rejectReport(reportId, true, input)

                ActionButtonType.REJECT ->
                    reportRepository.rejectReport(reportId, false, input)
            }

            result.onSuccess {
                _uiState.update { it.copy(isProcessing = false, processSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isProcessing = false, errorMessage = error.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
