package com.wheretogo.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.presentation.R

@Composable
fun ReportReason.toDisplayName(): String = when (this) {
    ReportReason.SPAM -> stringResource(R.string.report_spam)
    ReportReason.HARASSMENT -> stringResource(R.string.report_harassment)
    ReportReason.INAPPROPRIATE -> stringResource(R.string.report_inappropriate)
    ReportReason.VIOLENCE -> stringResource(R.string.report_violence)
    ReportReason.HATE_SPEECH -> stringResource(R.string.report_hate_speech)
    ReportReason.MISINFORMATION -> stringResource(R.string.report_misinformation)
    ReportReason.COPYRIGHT -> stringResource(R.string.report_copyright)
    ReportReason.OTHER -> stringResource(R.string.report_other)
}